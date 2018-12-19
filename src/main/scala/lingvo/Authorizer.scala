package lingvo

import client.{Body, Header, RestClient, Uri}
import configuration.LingvoProperties
import org.apache.http.HttpHeaders

import scala.concurrent.{ExecutionContext, Future, Promise}

case class AuthToken(value: String) extends AnyVal

class Authorizer(properties: LingvoProperties,
                 client: RestClient)
                (implicit ec: ExecutionContext) {

  import Authorizer._

  def auth(): Future[String] = {
    token match {
      case TokenSet(value) => Future.successful(value)
      case TokenRetrieving => promise.future
      case TokenNotSet => retrieve()
    }
  }

  def retrieve(): Future[String] = {
    this.synchronized {
      token = TokenRetrieving
      promise = Promise[String]()
    }

    val api = "/api/v1.1/authenticate"
    val header = Header(HttpHeaders.AUTHORIZATION, "Basic " + properties.apiKey)

    client
      .post(Uri(properties.serviceUrl + api), header)
      .map(_.bodyOpt match {
        case Some(Body(text)) =>
          this.synchronized(token = TokenSet(text))
          promise.success(text)
        case None =>
          promise.failure(new RuntimeException("no body"))
      })
      .recover { case ex => promise.failure(ex) }

    promise.future
  }

  @volatile
  private var token: TokenState = TokenNotSet

  @volatile
  private var promise = Promise[String]()

}

private object Authorizer {
  sealed trait TokenState
  case object TokenNotSet extends TokenState
  case object TokenRetrieving extends TokenState
  case class TokenSet(value: String) extends TokenState
}