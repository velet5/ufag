package http

import java.nio.charset.StandardCharsets

import http.Client.Response
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpUriRequest}
import org.apache.http.concurrent.FutureCallback
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.entity.StringEntity
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClientBuilder}
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.{Header, HttpResponse}
import org.slf4j.LoggerFactory

import scala.concurrent.{Future, Promise}
import scala.io.Source

trait Client {
  def get(uri: String, headers: Header*): Future[Response]
  def post(uri: String, headers: Header*): Future[Response]
  def post(uri: String, body: String, headers: Header*): Future[Response]
}

object Client {
  case class Response(status: Int, body: Option[String])
}

object ClientImpl {
  /** Java complains about Lingvo.com SSL certificate */
  private val sslContext =
    SSLContextBuilder.create()
      .loadTrustMaterial(new TrustSelfSignedStrategy())
      .build()

  private val config: RequestConfig = RequestConfig.custom().setConnectTimeout(500).build()
  private val client: CloseableHttpAsyncClient =
    HttpAsyncClientBuilder.create()
      .setDefaultRequestConfig(config)
      .setSSLContext(sslContext).build()
}

class ClientImpl extends Client {

  import ClientImpl._

  client.start()

  // ------------------------------- Api -------------------------------

  def get(uri: String, headers: Header*): Future[Response] = {
    val request = new HttpGet(uri)
    
    headers.foreach(request.setHeader)
    execute(request)
  }

  def post(uri: String, headers: Header*): Future[Response] =
    post0(uri, None, headers.toSeq)

  def post(uri: String, body: String, headers: Header*): Future[Response] =
    post0(uri, Some(body), headers.toSeq)


  // ------------------------------- Private -------------------------------

  private def post0(uri: String, body: Option[String], headers: Seq[Header]): Future[Response] = {
    val request = new HttpPost(uri)
    body
      .map(new StringEntity(_, StandardCharsets.UTF_8))
      .foreach(request.setEntity)

    headers.foreach(request.setHeader)
    execute(request)
  }


  private def execute(request: HttpUriRequest): Future[Response] = {
    log.info(s"Executing ${request.toString}")

    val promise = Promise[Response]()

    client.execute(request, new FutureCallback[HttpResponse] {
      override def completed(result: HttpResponse): Unit = {
        val encoding = StandardCharsets.UTF_8.name()
        val code = result.getStatusLine.getStatusCode
        val entity = Source.fromInputStream(result.getEntity.getContent, encoding).mkString
        val body = if (entity != null && entity.nonEmpty) Some(entity) else None

        promise.success(Response(code, body))
      }
      override def failed(ex: Exception): Unit = promise.failure(ex)
      override def cancelled(): Unit = promise.failure(new RuntimeException("cancelled"))
    })

    promise.future
  }

  private val log = LoggerFactory.getLogger(getClass)

}
