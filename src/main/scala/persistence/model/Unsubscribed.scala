package persistence.model

import scalikejdbc.WrappedResultSet

case class Unsubscribed(chatId: Long)

object Unsubscribed extends scalikejdbc.SQLSyntaxSupport[Unsubscribed] {
  override def schemaName: Option[String] = Some("ufag")
  override def tableName: String = "unsubscribed"

  def apply(rs: WrappedResultSet): Unsubscribed = Unsubscribed(rs.long(1))
}