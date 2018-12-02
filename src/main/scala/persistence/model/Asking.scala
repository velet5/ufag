package persistence.model

import scalikejdbc.WrappedResultSet

case class Asking(chatId: Long, originalMessageId: Long, ownerMessageId: Long)

object Asking extends scalikejdbc.SQLSyntaxSupport[Asking] {
  override def schemaName: Option[String] = Some("ufag")
  override def tableName: String = "asking" // yeah, lame

  def apply(rs: WrappedResultSet): Asking = Asking(rs.long(1), rs.long(2), rs.long(3))
}