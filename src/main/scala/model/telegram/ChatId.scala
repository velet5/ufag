package model.telegram

import util.circe.CirceCodec

case class ChatId(id: Long)

object ChatId {
  implicit val circeCodec: CirceCodec[ChatId] = CirceCodec.codecLong.imap(ChatId(_), _.id)
}