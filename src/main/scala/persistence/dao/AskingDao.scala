package persistence.dao

import org.slf4j.LoggerFactory
import persistence.model.Asking
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class AskingDao()
               (implicit ec: ExecutionContext,
                session: DBSession) {

  def find(chatId: Long, ownerMessageId: Long): Future[Option[Asking]] = {
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


  def save(chatId: Long, originalMessageId: Long, ownerMessageId: Long): Future[Unit] = {
    Future {
      DB.localTx { implicit session =>
        withSQL {
          insert.into(Asking).values(chatId, originalMessageId, ownerMessageId)
        }.execute()
      }.apply()
    }
  }

  private val log = LoggerFactory.getLogger(getClass)

}