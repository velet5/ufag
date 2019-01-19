package oxford.exception

import client.Request

class FailedRequestException(request: Request, val statusCode: Int) extends RuntimeException(
  s"Request $request resulted with code $statusCode"
)
