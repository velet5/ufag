package lingvo

import client.HttpMethod.GET
import client._
import com.fasterxml.jackson.databind.ObjectMapper
import common.exception.EmptyBodyException
import configuration.LingvoProperties
import org.apache.http.HttpHeaders.AUTHORIZATION
import persistence.model.Provider
import service.ArticleService
import util.TextUtils.isCyrillic
import util.UrlUtils.encode

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

class LingvoClient(
  properties: LingvoProperties,
  articleService: ArticleService,
  restClient: RestClient,
  mapper: ObjectMapper
)(
  implicit ec: ExecutionContext
) {

  import LingvoClient._

  def getTranslation(word: String): Future[String] =
    articleService
      .find(word, Provider.Lingvo)
      .flatMap(_.fold(translate(word))(Future successful _.content))

  def getRussianDefinition(word: String): Future[String] = {
    articleService
      .find(word, Provider.LingvoRu)
      .flatMap(_.fold(define(word))(Future successful _.content))
  }

  def getCorrections(word: String): Future[Option[Seq[String]]] =
    authorizedGet(Uri(properties.serviceUrl + "/api/v1/Suggests" + buildQuery(word)))
      .map(body => Try(mapper.readValue[Array[String]](body, classOf[Array[String]])).toOption)
      .map(_.map(_.toSeq))

  // private

  private def define(word: String): Future[String] =
    authorizedGet(Uri(
      properties.serviceUrl +
        "/api/v1/Translation" +
        s"?text=${encode(word)}&srcLang=$Russian&dstLang=$Russian"
    ))

  private def translate(word: String): Future[String] =
    authorizedGet(Uri(properties.serviceUrl + "/api/v1/Translation" + buildQuery(word)))

  private def buildQuery(word: String): String =
    if (isCyrillic(word))
      s"?text=${encode(word)}&srcLang=$Russian&dstLang=$English"
    else
      s"?text=${encode(word)}&srcLang=$English&dstLang=$Russian"

  private def authorizedGet(uri: Uri): Future[String] = {
    def go(token: String) =
      restClient.execute(Get(uri, Seq(Header(AUTHORIZATION, "Bearer " + token))))

    auth().flatMap(go).flatMap { response =>
      response.statusCode match {
        case 401 =>
          retrieve().flatMap(go)
        case _ =>
          Future.successful(response)
      }
    }
    .map(_.bodyOpt.map(_.value).getOrElse(throw new EmptyBodyException(GET, uri)))

  }

  private def auth(): Future[String] =
    token match {
      case TokenSet(value) => Future.successful(value)
      case TokenRetrieving => promise.future
      case TokenNotSet => retrieve()
    }

  private def retrieve(): Future[String] = {
    token = TokenRetrieving
    promise = Promise[String]()

    restClient
      .execute(authRequest)
      .map(handleAuthResponse)
      .recover { case ex => promise.failure(ex) }

    promise.future
  }

  private def handleAuthResponse(response: Response): Unit =
    response.bodyOpt match {
      case Some(Body(text)) =>
        token = TokenSet(text)
        promise.success(text)

      case None =>
        promise.failure(new RuntimeException("no body"))
    }

  @volatile
  private var token: TokenState = TokenNotSet

  @volatile
  private var promise = Promise[String]()

  private lazy val authRequest = Post(
    Uri(properties.serviceUrl + "/api/v1.1/authenticate"),
    Seq(Header(AUTHORIZATION, "Basic " + properties.apiKey)),
    None
  )

}

private object LingvoClient {

  sealed trait TokenState
  case object TokenNotSet extends TokenState
  case object TokenRetrieving extends TokenState
  case class TokenSet(value: String) extends TokenState

  private val English = 1033
  private val Russian = 1049

}
