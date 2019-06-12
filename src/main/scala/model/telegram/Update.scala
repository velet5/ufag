package model.telegram

import io.circe.generic.extras.ConfiguredJsonCodec
import model.telegram.Update._
import util.circe.CirceCodec
import model.JsonConfig.snakeCase

@ConfiguredJsonCodec
case class Update(
  message: Option[Message]
)

object Update {

  @ConfiguredJsonCodec
  case class Chat(
    id: ChatId,
  )

  case class ChatId(id: Long)

  object ChatId {
    implicit val circeCodec: CirceCodec[ChatId] = CirceCodec.codecLong.imap(ChatId(_), _.id)
  }

  @ConfiguredJsonCodec
  case class Message(
    messageId: Long,
    chat: Chat,
    text: Option[String],
  )

}
