package persistence.dao

import persistence.model.Article
import persistence.model.Provider.Provider
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class ArticleDao()
                (implicit ec: ExecutionContext,
                 session: DBSession) {

  import scalikejdbc.{SQLSyntax => sql}

  def find(text: String, provider: Provider): Future[Option[Article]] = Future {
    val a = Article.syntax("a")
    DB.readOnly {implicit session =>
      withSQL {
        select
          .from(Article as a)
          .where
          .eq(a.searchText, text)
          .and
          .eq(a.provider, provider.id)
      }
    }.map(Article(_)).single().apply()
  }

  def save(searchText: String, content: String, provider: Provider): Future[Unit] = {
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Article).values(searchText, content, provider.id)
        }.execute()
      }.apply()
    }
  }

  def count(): Future[Int] = Future {
    val a = Article.syntax("a")
    withSQL(select(sql.count).from(Article as a))
      .map(rs => rs.int(1))
      .single()
      .apply()
      .getOrElse(0)
  }

}