package lingvo

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configuration.Configuration
import http.Client
import lingvo.LingvoProcessor.{EmptyResult, ServiceError, UnknownResponse}
import org.apache.http.HttpHeaders
import org.apache.http.message.BasicHeader
import persistence.Db
import persistence.Db.Provider
import util.WordSimilarity

import scala.concurrent.{Future, Promise}
import scala.util.Try

class Lingvo(client: Client, db: Db) {

  // En-Ru (1033 → 1049)

  import scala.concurrent.ExecutionContext.Implicits.global

  private val ApiKey = Configuration.properties.lingvo.apiKey
  private val English = 1033
  private val Russian = 1049
  private val ServiceUrl = "https://developers.lingvolive.com"

  private val processor = new LingvoProcessor

  private val cyrillic =
    "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
    "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"

  private val mapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_NULL)

  @volatile
  private var token: TokenState = TokenNotSet

  sealed trait TokenState
  case object TokenNotSet extends TokenState
  case object TokenRetrieving extends TokenState
  case class TokenSet(value: String) extends TokenState

  sealed trait TranslationResult
  case class TranslationArticle(text: String) extends TranslationResult
  case object NeedAuth extends TranslationResult

  private val emptyTranslation = "Я не знаю такого слова"
  private val emptyResult: Either[String, String] = Left(emptyTranslation)

  @volatile
  private var promise = Promise[String]()

  def auth(): Future[String] = {
    def get(): Future[String] = {
      promise = Promise[String]()
      token = TokenRetrieving
      val api = "/api/v1.1/authenticate"
      val header = new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + ApiKey)

      client.post(ServiceUrl + api, header).foreach {value =>
        value.body.foreach {text =>
          promise.success(text)
          token = TokenSet(text)
        }
      }

      promise.future
    }

    token match {
      case TokenSet(value) => Future.successful(value)
      case TokenRetrieving => promise.future
      case TokenNotSet => get()
    }
  }

  private def getArticle(
    text: String,
    provider: Provider.Value,
    fetcher: (String, String) => Future[TranslationResult],
    onEmptyResult: String => Future[Either[String, String]] = _ => Future.successful(emptyResult)
  ): Future[Either[String, String]] = {
    def innerTranslate(): Future[String] = auth()
      .flatMap(fetcher(_, text))
      .flatMap {
        case NeedAuth =>
          token = TokenNotSet
          innerTranslate()

        case TranslationArticle(article) =>
          Future.successful(article)
      }

    db
      .getArticle(text, provider)
      .flatMap {
        case Some(value) => Future.successful(value.content)
        case None => innerTranslate()
      }
      .flatMap {string =>
        processor.process(string) match {
          case Right(markdown) =>
            db.saveArticle(text, string, provider)
            Future.successful(Right(markdown))

          case Left(EmptyResult) =>
            onEmptyResult(text)

          case Left(UnknownResponse) =>
            Future.successful(Left("*Неизвестный ответ сервиса переводов*"))

          case Left(ServiceError) =>
            Future.successful(Left("*Ошибка работы сервиса*"))
        }
      }
  }


  private def correct(text: String): Future[Either[String, String]] =
    auth()
      .flatMap(fetchSuggestions(_, text))
      .map {
        case Array() | null =>
          Left("*Ничего не найдено*")
        case array =>
          Left("Возможно вы имели в виду: \n" +
            array
              .sortBy(WordSimilarity.calculate(text, _))
              .take(5)
              .map("*" + _ + "*")
              .mkString("\n"))
      }

  def defineRussian(text: String): Future[Either[String, String]] =
    getArticle(text, Provider.LingvoRu, fetchRussian)

  def translate(text: String): Future[Either[String, String]] =
     getArticle(text, Provider.Lingvo, fetchTranslation, correct)

  private def fetchArticle(
    token: String,
    rawText: String,
    from: Int,
    to: Int): Future[TranslationResult] = {
    //GET api/v1/Translation?
    val api = "/api/v1/Translation"
    val text = URLEncoder.encode(rawText, StandardCharsets.UTF_8.name()).replace("+", "%20")
    val query = s"?text=$text&srcLang=$from&dstLang=$to"
    val uri = ServiceUrl + api + query
    val header = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)

    client.get(uri, header).map {response =>
      response.status match {
        case 401 =>
          NeedAuth
        case _ =>
          TranslationArticle(response.body.getOrElse(emptyTranslation))
      }
    }
  }

  private def fetchRussian(token: String, rawText: String): Future[TranslationResult] =
    fetchArticle(token, rawText, Russian, Russian)

  private def fetchTranslation(token: String, rawText: String): Future[TranslationResult] = {
    val (from, to) = if (isCyrillic(rawText)) (Russian, English) else (English, Russian)

    fetchArticle(token, rawText, from, to)
  }

  private def fetchSuggestions(token: String, rawText: String): Future[Array[String]] = {
    val api = "/api/v1/Suggests"
    val text = URLEncoder.encode(rawText, StandardCharsets.UTF_8.name()).replace("+", "%20")
    val query =
      if (isCyrillic(rawText)) {
        s"?text=$text&srcLang=$Russian&dstLang=$English"
      } else {
        s"?text=$text&srcLang=$English&dstLang=$Russian"
      }

    val uri = ServiceUrl + api + query
    val header = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)

    client
      .get(uri, header)
      .map(_.body.flatMap(body => Try(mapper.readValue[Array[String]](body, classOf[Array[String]])).toOption))
      .map(_.getOrElse(Array.empty))
  }

  private def isCyrillic(text: String): Boolean =
    text.exists(cyrillic.indexOf(_) >= 0)

}
