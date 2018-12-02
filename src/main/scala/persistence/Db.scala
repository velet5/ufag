package persistence

import java.time.ZonedDateTime

import configuration.Configuration
import org.slf4j.LoggerFactory
import persistence.model._
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

object Db {

  private val properties = Configuration.properties.postgres

  Class.forName(classOf[org.postgresql.Driver].getCanonicalName)
  ConnectionPool.singleton("jdbc:postgresql://localhost:5432/ufag", properties.user, properties.password)

  private implicit val session: AutoSession.type = AutoSession

}

class Db(implicit ec: ExecutionContext) {

  import Db._

  private val log = LoggerFactory.getLogger(getClass)

  def rememberQuery(chatId: Long, text: String, time: ZonedDateTime, messageId: Long): Future[Unit] = {
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Query).values(chatId, text, time, messageId)
        }.execute()
      }.apply()
    }
  }

  def get(chatId: Long, text: String): Future[Option[Query]] = {
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

  def getArticle(text: String, provider: Provider.Value): Future[Option[Article]] = Future {
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


  def saveArticle(searchText: String, content: String, provider: Provider.Value): Future[Unit] = {
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Article).values(searchText, content, provider.id)
        }.execute()
      }.apply()
    }
  }

  def fetchStat(): Future[Stat] = {
    import sqls.{count, distinct}

    val q = Query.syntax("q")
    val a = Article.syntax("a")

    Future {
      DB.readOnly {implicit session =>
        val (userCount, queryCount) = withSQL {
          select(count(distinct(q.chatId)), count).from(Query as q)
        }.map(rs => (rs.int(1), rs.int(2))).single().apply().getOrElse((0, 0))

        val wordCount = withSQL {
          select(count).from(Article as a)
        }.map(_.int(1)).single().apply().getOrElse(0)

        Stat(userCount, queryCount, wordCount)
      }
    }
  }

  def getAsking(chatId: Long, ownerMessageId: Long): Future[Option[Asking]] = {
    val a = Asking.syntax("a")
    val tried = Future {
      DB.readOnly {implicit session =>
        withSQL {
          select
            .from(Asking as a)
            .where.eq(a.chatId, chatId)
            .and.eq(a.ownerMessageId, ownerMessageId)
        }
      }.map(Asking(_)).single().apply()
    }

    tried.failed.foreach(log.error("asking error", _))

    tried
  }
  

  def saveAsking(chatId: Long, originalMessageId: Long, ownerMessageId: Long): Future[Unit] = {
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Asking).values(chatId, originalMessageId, ownerMessageId)
        }.execute()
      }.apply()
    }
  }

  def subscribe(chatId: Long): Future[Unit] =
    Future {
      val u = Unsubscribed.syntax("u")

      DB.localTx { implicit session =>
        withSQL {
          delete.from(Unsubscribed).where.eq(u.chatId, chatId)
        }.update().apply()
      }
    }

  def unsubscribe(chatId: Long): Future[Unit] =
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Unsubscribed).values(chatId)
        }.update().apply()
      }
    }

}
