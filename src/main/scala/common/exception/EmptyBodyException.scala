package common.exception

import client.{HttpMethod, Request, Uri}

class EmptyBodyException(httpMethod: HttpMethod, uri: Uri) extends RuntimeException(
  s"Request $httpMethod $uri resulted in empty body"
) {
  def this(request: Request) = this(request.method, request.uri)
}