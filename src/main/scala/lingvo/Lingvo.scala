package lingvo

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import client.{Header, RestClient, Uri}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configuration.LingvoProperties
import lingvo.LingvoProcessor.{EmptyResult, ServiceError, UnknownResponse}
import org.apache.http.HttpHeaders
import persistence.model.Provider
import service.ArticleService
import util.TextUtils.isCyrillic
import util.WordSimilarity

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Lingvo(authorizer: Authorizer,
             properties: LingvoProperties,
             client: RestClient,
             articleService: ArticleService,
             processor: LingvoProcessor)
            (implicit ec: ExecutionContext) {

  import Lingvo._

  private val mapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_NULL)

  private def getArticle(
    text: String,
    provider: Provider.Value,
    fetcher: (String, String) => Future[TranslationResult],
    onEmptyResult: String => Future[Either[String, String]] = _ => Future.successful(emptyResult)
  ): Future[Either[String, String]] = {
    def innerTranslate(): Future[String] = authorizer.auth()
      .flatMap(fetcher(_, text))
      .flatMap {
        case NeedAuth =>
          authorizer.retrieve().flatMap(_ => innerTranslate())

        case TranslationArticle(article) =>
          Future.successful(article)
      }

    articleService
      .find(text, provider)
      .flatMap {
        case Some(value) => Future.successful(value.content)
        case None =>
          for {
            string <- innerTranslate()
            _ <- articleService.save(text, string, provider) // TODO: save after processing
          } yield string
      }
      .flatMap {string =>
        processor.process(string) match {
          case Right(markdown) =>
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
    authorizer.auth()
      .flatMap(fetchSuggestions(_, text))
      .map {
        case Array() | null =>
          Left("*Ничего не найдено*")
        case array =>
          Left("Возможно вы имели в виду: \n" +
            array
              .sortBy(WordSimilarity.calculate(text, _))
              .take(5)
              .map(word => s"*$word*")
              .mkString("\n"))
      }

  def defineRussian(text: String): Future[Either[String, String]] =
    getArticle(text, Provider.LingvoRu, fetchRussian)

  def translate(text: String): Future[Either[String, String]] =
     getArticle(text, Provider.Lingvo, fetchTranslation, correct)

  private def fetchArticle(token: String, rawText: String, from: Int, to: Int): Future[TranslationResult] = {
    val api = "/api/v1/Translation"
    val text = URLEncoder.encode(rawText, StandardCharsets.UTF_8.name()).replace("+", "%20")
    val query = s"?text=$text&srcLang=$from&dstLang=$to"
    val uri = properties.serviceUrl + api + query
    val header = Header(HttpHeaders.AUTHORIZATION, "Bearer " + token)

    client.get(Uri(uri), header).map {response =>
      response.statusCode match {
        case 401 =>
          NeedAuth
        case _ =>
          TranslationArticle(response.bodyOpt.map(_.value).getOrElse(emptyTranslation))
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

    val uri = properties.serviceUrl + api + query
    val header = Header(HttpHeaders.AUTHORIZATION, "Bearer " + token)

    client
      .get(Uri(uri), header)
      .map(_.bodyOpt.flatMap(body => Try(mapper.readValue[Array[String]](body.value, classOf[Array[String]])).toOption))
      .map(_.getOrElse(Array.empty))
  }

}

object Lingvo {

  private val English = 1033
  private val Russian = 1049

  sealed trait TranslationResult
  case class TranslationArticle(text: String) extends TranslationResult
  case object NeedAuth extends TranslationResult

  private val emptyTranslation = "Я не знаю такого слова"
  private val emptyResult: Either[String, String] = Left(emptyTranslation)

}