package persistence.model

import java.time.ZonedDateTime

import scalikejdbc.WrappedResultSet

case class Query(chatId: Long, text: String, time: ZonedDateTime, messageId: Long)

object Query extends scalikejdbc.SQLSyntaxSupport[Query] {

  override def schemaName: Option[String] = Some("ufag")
  override val tableName = "queries"

  def apply(rs: WrappedResultSet): Query = Query(rs.long(1), rs.string(2), rs.zonedDateTime(3), rs.long(4))
}
