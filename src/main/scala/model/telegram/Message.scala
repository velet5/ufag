package model.telegram

import io.circe.generic.extras.ConfiguredJsonCodec
import model.telegram.Message.Chat
import model.JsonConfig.snakeCase

@ConfiguredJsonCodec
case class Message(
  messageId: Long,
  chat: Chat,
  text: Option[String],
)

object Message {

  @ConfiguredJsonCodec
  case class Chat(id: ChatId)

}
