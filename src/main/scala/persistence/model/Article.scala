package persistence.model

import scalikejdbc.WrappedResultSet

case class Article(searchText: String, content: String, provider: Provider.Value)

object Article extends scalikejdbc.SQLSyntaxSupport[Article] {
  override def schemaName: Option[String] = Some("ufag")
  override def tableName: String = "articles"

  def apply(rs: WrappedResultSet): Article = Article(rs.string(1), rs.string(2), Provider(rs.int(3)))
}
