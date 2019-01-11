package persistence.dao

import persistence.model.Provider.Provider
import persistence.model.{Article, ArticleTable}
import slick.dbio.Effect.{Read, Write}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class ArticleDao()
                (implicit ec: ExecutionContext) {

  import ArticleTable._

  def find(text: String, provider: Provider): DBIOAction[Option[Article], NoStream, Read] = {
    ArticleTable.articles
      .filter(_.searchText === text.bind)
      .filter(_.provider === provider.bind)
      .result
      .headOption
      .map(_.map {case (searchText, content, prov) =>
        Article(searchText, content, prov) // FIXME: wtf is this
      })
  }

  def save(searchText: String, content: String, provider: Provider): DBIOAction[Int, NoStream, Write] = {
    ArticleTable.articles += (searchText, content, provider)
  }

}