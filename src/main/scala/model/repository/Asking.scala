package model.repository

import model.telegram.{Chat, Message}
import slick.jdbc.PostgresProfile.api.{Query => _, _}

case class Asking(
  chatId: Chat.Id,
  originalMessageId: Message.Id,
  ownerMessageId: Message.Id,
)

class AskingTable(tag: Tag) extends Table[Asking](tag, "ufag") {
  def chatId = column[Chat.Id]("chat_id")
  def originalMessageId = column[Message.Id]("original_message_id")
  def ownerMessageId = column[Message.Id]("owner_message_id")

  override def * =
    (chatId, originalMessageId, ownerMessageId) <> (Asking.tupled, Asking.unapply)
}


