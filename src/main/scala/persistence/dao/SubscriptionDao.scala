package persistence.dao

import persistence.model.Unsubscribed
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDao()
                     (implicit ec: ExecutionContext,
                      session: DBSession) {

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
