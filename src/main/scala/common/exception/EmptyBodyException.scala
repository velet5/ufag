package oxford.exception

import client.Request

class EmptyBodyException(request: Request) extends RuntimeException(
  s"Request $request result in empty body"
)