package model.telegram

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

@ConfiguredJsonCodec
case class Response[A](
  ok: Boolean,
  value: Option[A],
  description: Option[String]
)

object Response {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

}
