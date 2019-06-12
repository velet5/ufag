package model.telegram

import io.circe.generic.extras.ConfiguredJsonCodec
import model.JsonConfig.snakeCase

@ConfiguredJsonCodec
case class Update(
  message: Option[Message]
)
