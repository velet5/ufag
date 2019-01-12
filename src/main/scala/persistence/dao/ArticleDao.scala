package persistence.dao

import persistence.Tables
import persistence.model.Article
import persistence.model.Provider.Provider
import slick.dbio.Effect.{Read, Write}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class ArticleDao()
                (implicit ec: ExecutionContext) {

  def find(text: String, provider: Provider): DBIOAction[Option[Article], NoStream, Read] =
    Tables.articles
      .filter(_.searchText === text.bind)
      .filter(_.provider === provider.bind)
      .result
      .headOption

  def save(article: Article): DBIOAction[Int, NoStream, Write] =
    Tables.articles += article

}