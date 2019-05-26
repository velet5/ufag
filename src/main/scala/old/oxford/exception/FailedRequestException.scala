package old.oxford.exception

import old.client.Request

class FailedRequestException(request: Request, val statusCode: Int) extends RuntimeException(
  s"Request $request resulted with code $statusCode"
)
