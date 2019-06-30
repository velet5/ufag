package model.telegram

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import model.JsonConfig
import model.telegram.Message.ReplyToMessage
import slick.lifted.Isomorphism
import util.circe.CirceCodec

@ConfiguredJsonCodec
case class Message(
  messageId: Message.Id,
  chat: Chat,
  text: Option[String],
  replyToMessage: Option[ReplyToMessage],
)

object Message {

  implicit val jsonConfig: Configuration = JsonConfig.snakeCase

  case class Id(value: Long)

  object Id {
    implicit val codec: CirceCodec[Id] = CirceCodec.codecLong.imap(Id(_), _.value)
    implicit val isomorphism: Isomorphism[Id, Long] = new Isomorphism(_.value, Id(_))
  }

  @ConfiguredJsonCodec
  case class ReplyToMessage(
    messageId: Message.Id,
  )

}
