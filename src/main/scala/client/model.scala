package client

import java.nio.charset.StandardCharsets

import client.HttpMethod.{GET, POST}
import org.apache.http.HttpEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHeader

import scala.io.Source

sealed trait Request {
  def uri: Uri
  def headers: Seq[Header]
  def method: HttpMethod = this match {
    case _: Get => GET
    case _: Post => POST
  }
}

case class Get(uri: Uri, headers: Seq[Header]) extends Request
case class Post(uri: Uri, headers: Seq[Header], bodyOpt: Option[Body]) extends Request

case class Response(statusCode: Int,
                    bodyOpt: Option[Body])

case class Uri(value: String) extends AnyVal
case class Body(value: String) extends AnyVal
case class Header(name: String, value: String)

sealed trait HttpMethod

object HttpMethod {
  case object GET extends HttpMethod
  case object POST extends HttpMethod
}

object Response {
  implicit val fromHttpResponse: FromHttpResponse[Response] =
    response => {
      val statusCode = response.getStatusLine.getStatusCode
      val bodyOpt =
        Option(response.getEntity)
          .map(_.getContent)
          .map(Source.fromInputStream(_, "UTF-8"))
          .map(_.mkString)
          .map(Body)

      Response(statusCode, bodyOpt)
    }

}

object Request {
  implicit val toHttpRequest: ToHttpRequest[Request] = {
    case Get(uri, headers) =>
      val get = new HttpGet(uri.value)
      get.setHeaders(toHttpHeaders(headers))
      get

    case Post(uri, headers, None) =>
      val post = new HttpPost(uri.value)
      post.setHeaders(toHttpHeaders(headers))
      post

    case Post(Uri(uri), headers, Some(Body(body))) =>
      val post = new HttpPost(uri)
      post.setHeaders(toHttpHeaders(headers))
      post.setEntity(toHttpEntity(body))
      post
  }

  private def toHttpHeaders(headers: Seq[Header]): Array[org.apache.http.Header] =
    headers.map(toHttpHeader).toArray

  private def toHttpEntity(text: String): HttpEntity =
    new StringEntity(text, StandardCharsets.UTF_8)

  private def toHttpHeader(header: Header): org.apache.http.Header =
    new BasicHeader(header.name, header.value)
}
