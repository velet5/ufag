package model.telegram

import io.circe.Decoder
import io.circe.generic.extras.ConfiguredJsonCodec
import model.JsonConfig.snakeCase

@ConfiguredJsonCodec
case class Response[A](
  ok: Boolean,
  value: Option[A],
  description: Option[String]
)

object Response {



}
