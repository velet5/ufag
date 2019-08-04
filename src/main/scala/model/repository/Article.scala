package model.repository

import cats.syntax.option.catsSyntaxOptionId
import enumeratum.values.{IntEnum, IntEnumEntry}
import model.repository.Article._
import slick.jdbc.PostgresProfile.api._

import scala.collection.immutable

case class Article(searchText: String, content: String, provider: Provider)

object Article {

  abstract sealed class Provider(val value: Int) extends IntEnumEntry

  object Provider extends IntEnum[Provider] {

    case object Lingvo extends Provider(1)

    case object Oxford extends Provider(2)

    case object LingvoRu extends Provider(3)

    override val values: immutable.IndexedSeq[Provider] = findValues

    // FIXME: get rid of exception
    implicit val isomorphism: Isomorphism[Provider, Int] =
      new Isomorphism(_.value, this.valuesToEntriesMap.getOrElse(_, throw new RuntimeException("Unmapped value")))
  }

}

class ArticleTable(tag: Tag) extends Table[Article](tag, "ufag".some, "articles") {

  def searchText = column[String]("search_text")

  def content = column[String]("content")

  def provider = column[Provider]("provider")

  override def * =
    (searchText, content, provider) <> ((Article.apply _).tupled, Article.unapply)
}

