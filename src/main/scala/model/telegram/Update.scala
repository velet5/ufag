package model.telegram

import io.circe.derivation.renaming.snakeCase
import io.circe.{Decoder, Encoder}
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive

@derive(decoder(snakeCase), encoder(snakeCase))
case class Update(
  chat: Chat,
 // message: Option[Message]
)

@derive(decoder(snakeCase), encoder(snakeCase))
case class Chat(
  id: ChatId,
)

case class ChatId(id: Long)

object ChatId {
  implicit val decoder: Decoder[ChatId] = Decoder.decodeLong.map(ChatId(_))
  implicit val encoder: Encoder[ChatId] = Encoder.encodeLong.contramap(_.id)
}

@derive(decoder(snakeCase), encoder(snakeCase))
case class Message(
  messageId: Long,
  chat: Chat,
  replyToMessage: Option[Message],
  text: Option[String],
)
