package client

import cats.syntax.option.catsSyntaxOptionId
import org.slf4j.LoggerFactory

import scala.concurrent.Future

trait RestClient {
  def get(uri: Uri, header: Header): Future[Response] = execute(Get(uri, Seq(header)))
  def get(uri: Uri, headers: Seq[Header]): Future[Response] = execute(Get(uri, headers))
  def post(uri: Uri, header: Header): Future[Response] = execute(Post(uri, Seq(header), None))
  def post(uri: Uri, header: Header, body: Body): Future[Response] = execute(Post(uri, Seq(header), body.some))
  def execute(request: Request): Future[Response]
}

class RestClientImpl(httpClient: HttpClient[Request, Response]) extends RestClient {
  override def execute(request: Request): Future[Response] = {
    log.info(s"Execution http request ${Request.str(request)}")
    httpClient.execute(request)
  }

  // internal

  private val log = LoggerFactory.getLogger(this.getClass)
}
