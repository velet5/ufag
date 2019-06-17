package model.repository

import java.time.ZonedDateTime

import cats.syntax.option._
import slick.jdbc.PostgresProfile.api.{Query => _, _}
import util.slick.Mappers._

case class Query(chatId: Long, text: String, time: ZonedDateTime, messageId: Long)

class QueryTable(tag: Tag) extends Table[Query](tag, "ufag".some, "queries") {
  def chatId = column[Long]("chat_id")
  def text = column[String]("text")
  def time = column[ZonedDateTime]("time")
  def messageId = column[Long]("message_id")

  override def * = (chatId, text, time, messageId) <> (Query.tupled, Query.unapply)
}

