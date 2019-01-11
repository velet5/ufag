package persistence.dao

import java.time.ZonedDateTime

import persistence.model.{Article, Query}
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class QueryDao()
              (implicit ec: ExecutionContext,
               session: DBSession) {

  import scalikejdbc.{SQLSyntax => sql}

  def save(chatId: Long, text: String, time: ZonedDateTime, messageId: Long): Future[Unit] = {
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Query).values(chatId, text, time, messageId)
        }.execute()
      }.apply()
    }
  }

  def find(chatId: Long, text: String): Future[Option[Query]] = {
    val q = Query.syntax("q")
    val now = ZonedDateTime.now()
    val monthAgo = now.minusMonths(1)

    Future {
      DB.readOnly { implicit session =>
        withSQL {
          select
            .from(Query as q)
            .where
            .eq(q.chatId, chatId)
            .and.eq(q.text, text)
            .and.gt(q.time, monthAgo)
            .orderBy(q.time).desc
        }
      }.map(Query(_)).first().apply()
    }
  }

  def count(): Future[Int] = Future {
    val q = Query.syntax("a")
    withSQL(select(sql.count).from(Query as q))
      .map(rs => rs.int(1))
      .single()
      .apply()
      .getOrElse(0)
  }

}
