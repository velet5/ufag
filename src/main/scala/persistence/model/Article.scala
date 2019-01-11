package persistence.model

import persistence.model.Provider.Provider
import scalikejdbc.WrappedResultSet
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

case class Article(searchText: String, content: String, provider: Provider.Value)

object Article extends scalikejdbc.SQLSyntaxSupport[Article] {
  override def schemaName: Option[String] = Some("ufag")
  override def tableName: String = "articles"

  def apply(rs: WrappedResultSet): Article = Article(rs.string(1), rs.string(2), Provider(rs.int(3)))
}

class ArticleTable(tag: Tag) extends Table[(String, String, Provider)](tag, "articles") {
  import ArticleTable._

  def searchText = column[String]("search_text")
  def content= column[String]("content")
  def provider = column[Provider]("provider")

  override def * = (searchText, content, provider)
}

object ArticleTable {

  val articles = TableQuery[ArticleTable]

  // todo: move to common package to abstract over other (potential) enums
  implicit val providerMapper: JdbcType[Provider] = MappedColumnType.base[Provider, String](
    e => e.toString,
    s => Provider.withName(s)
  )

}