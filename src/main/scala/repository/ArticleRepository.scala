package repository

import io.circe.{Encoder, Printer}
import io.circe.syntax._
import model.repository.Article.Provider
import model.repository.{Article, ArticleTable}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api.{Query => _, _}
import slick.lifted.TableQuery

trait ArticleRepository[F[_]] {

  def find(text: String, provider: Provider): F[Option[Article]]

  def save[A : Encoder](word: String, value: A, provider: Provider): F[Int]

}

object ArticleRepository {

  def create: ArticleRepository[DBIO] = new Impl

  private class Impl extends ArticleRepository[DBIO] with Repository[DBIO, ArticleTable] {

    override def tableQuery = TableQuery[ArticleTable]

    override def find(text: String, provider: Provider): DBIO[Option[Article]] =
      tableQuery
      .filter(_.searchText === text)
      .filter(_.provider === provider)
      .result
      .headOption

    override def save[A: Encoder](
      word: String,
      value: A,
      provider: Provider
    ): DBIO[Int] =
      tableQuery += Article(
        searchText = word,
        content = value.asJson.pretty(Printer.noSpaces),
        provider = provider
      )
    
  }
}

