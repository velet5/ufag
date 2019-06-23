package model.telegram

import io.circe.generic.extras.ConfiguredJsonCodec
import model.JsonConfig.snakeCase

@ConfiguredJsonCodec
case class Forwarding(
  chatId: Chat.Id,
  fromChatId: Chat.Id,
  messageId: Message.Id,
)
