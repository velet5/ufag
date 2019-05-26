package model.telegram

import io.circe.derivation.renaming.snakeCase
import model.telegram.TelegramUpdate.Chat
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive

@derive(decoder(snakeCase), encoder(snakeCase))
case class TelegramUpdate(
  chat: Chat
)

object TelegramUpdate {

  @derive(decoder(snakeCase), encoder(snakeCase))
  case class Chat(
    id: Long
  )

  @derive(decoder(snakeCase), encoder(snakeCase))
  case class Message(
    messageId: Long,
    chat: Chat,
    replyToMessage: Option[Message],
  )

}
