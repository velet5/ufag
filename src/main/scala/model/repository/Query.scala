package model.repository

import java.time.ZonedDateTime

import cats.syntax.option._
import model.telegram.{Chat, Message}
import slick.jdbc.PostgresProfile.api.{Query => _, _}
import util.slick.Mappers._

case class Query(
  chatId: Chat.Id,
  text: String,
  time: ZonedDateTime,
  messageId: Message.Id,
)

class QueryTable(tag: Tag) extends Table[Query](tag, "ufag".some, "queries") {
  def chatId = column[Chat.Id]("chat_id")
  def text = column[String]("text")
  def time = column[ZonedDateTime]("time")
  def messageId = column[Message.Id]("message_id")

  override def * =
    (chatId, text, time, messageId) <> (Query.tupled, Query.unapply)
}

