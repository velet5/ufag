package persistence.model

import persistence.model.Provider.Provider
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

case class Article(searchText: String, content: String, provider: Provider.Value)

class ArticleTable(tag: Tag) extends Table[Article](tag, "articles") {
  import ArticleTable._

  def searchText = column[String]("search_text")
  def content= column[String]("content")
  def provider = column[Provider]("provider")

  override def * = (searchText, content, provider) <> (Article.tupled, Article.unapply)
}

object ArticleTable {

  val articles = TableQuery[ArticleTable]

  // todo: move to common package to abstract over other (potential) enums
  implicit val providerMapper: JdbcType[Provider] = MappedColumnType.base[Provider, String](
    e => e.toString,
    s => Provider.withName(s)
  )

}