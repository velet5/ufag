package oxford.exception

import client.Request

class FailedRequestException(request: Request, statusCode: Int) extends RuntimeException(
  s"Request $request resulted with code $statusCode"
)
