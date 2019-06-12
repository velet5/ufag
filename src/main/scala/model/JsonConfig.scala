package model

import io.circe.generic.extras.Configuration

object JsonConfig {

  implicit val snakeCase: Configuration = Configuration.default.withSnakeCaseMemberNames

}
