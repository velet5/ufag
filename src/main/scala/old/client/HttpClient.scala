package old.client

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.concurrent.FutureCallback
import org.apache.http.nio.client.HttpAsyncClient

import scala.concurrent.{Future, Promise}

trait HttpClient[Req, Res] {
  def execute(req: Req): Future[Res]
}

trait ToHttpRequest[A] {
  def run(a: A): HttpUriRequest
}

trait FromHttpResponse[A] {
  def run(response: HttpResponse): A
}

class HttpClientImpl[Req: ToHttpRequest, Res: FromHttpResponse](httpAsyncClient: HttpAsyncClient) extends HttpClient[Req, Res] {
  override def execute(req: Req): Future[Res] = {
    val promise = Promise[Res]()

    httpAsyncClient.execute(
      implicitly[ToHttpRequest[Req]].run(req),
      callback(promise))

    promise.future
  }

  private def callback(promise: Promise[Res]): FutureCallback[HttpResponse] =
    new FutureCallback[HttpResponse] {
      override def completed(result: HttpResponse): Unit = promise.success(implicitly[FromHttpResponse[Res]].run(result))
      override def failed(ex: Exception): Unit = promise.failure(ex)
      override def cancelled(): Unit = promise.failure(new RuntimeException("Request cancelled"))
    }
}
