package old.persistence.model

import cats.syntax.option.catsSyntaxOptionId
import slick.jdbc.PostgresProfile.api._

case class Asking(chatId: Long, originalMessageId: Long, ownerMessageId: Long)

class AskingTable(tag: Tag) extends Table[Asking](tag, "ufag".some, "asking") {
  def chatId = column[Long]("chat_id")
  def originalMessageId = column[Long]("original_message_id")
  def ownerMessageId = column[Long]("owner_message_id")

  override def * = (chatId, originalMessageId, ownerMessageId) <> (Asking.tupled, Asking.unapply)
}
