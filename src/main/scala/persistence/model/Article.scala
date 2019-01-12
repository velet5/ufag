package persistence.model

import cats.syntax.option.catsSyntaxOptionId
import persistence.model.Provider.Provider
import slick.jdbc.PostgresProfile.api._

case class Article(searchText: String, content: String, provider: Provider.Value)

class ArticleTable(tag: Tag) extends Table[Article](tag, "ufag".some, "articles") {

  def searchText = column[String]("search_text")
  def content= column[String]("content")
  def provider = column[Provider]("provider")

  override def * = (searchText, content, provider) <> (Article.tupled, Article.unapply)
}
