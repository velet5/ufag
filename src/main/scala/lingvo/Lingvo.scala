package lingvo

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import configuration.Configuration
import http.Client
import org.apache.http.HttpHeaders
import org.apache.http.message.BasicHeader
import persistence.Db
import persistence.Db.Provider

import scala.concurrent.{Future, Promise}

//object Lingvo extends App {
//
//  val lingvo = new Lingvo(null, null, new Settings)
//
//  println(lingvo.isCyrillic("папа"))
//  println(lingvo.isCyrillic("dad"))
//
//}

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

  def translate(text: String): Future[String] = {
    def innerTranslate(): Future[String] = auth()
      .flatMap(fetchTranslation(_, text))
      .flatMap {
        case NeedAuth =>
          token = TokenNotSet
          innerTranslate()

        case TranslationArticle(article) =>
          Future.successful(article)
      }

    db
      .getArticle(text, Provider.Lingvo)
      .flatMap {
        case Some(value) => Future.successful(value.content)
        case None => innerTranslate()
      }
      .map {string =>
        processor.process(string) match {
          case Right(markdown) =>
            println("RIGHT")
            println(markdown)
            db.saveArticle(text, string, Provider.Lingvo)
            markdown
          case Left(message) =>
            println("LEFT")
            println(message)
            message
        }
      }
  }

  private def fetchTranslation(token: String, rawText: String): Future[TranslationResult] = {
    //GET api/v1/Translation?
    val api = "/api/v1/Translation"
    val text = URLEncoder.encode(rawText, StandardCharsets.UTF_8.name()).replace("\\+", "%20")
    val query =
      if (isCyrillic(rawText)) {
        s"?text=$text&srcLang=$Russian&dstLang=$English"
      } else {
        s"?text=$text&srcLang=$English&dstLang=$Russian"
      }
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

  private def isCyrillic(text: String): Boolean = {
    text.exists(cyrillic.indexOf(_) >= 0)
  }

}
