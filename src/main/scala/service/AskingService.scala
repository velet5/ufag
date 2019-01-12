package service

import persistence.Db
import persistence.dao.AskingDao
import persistence.model.Asking

import scala.concurrent.Future

class AskingService(db: Db, askingDao: AskingDao) {

  def find(chatId: Long, ownerMessageId: Long): Future[Option[Asking]] =
    db.run(askingDao.find(chatId, ownerMessageId))

  def save(chatId: Long, originalMessageId: Long, ownerMessageId: Long): Future[Int] =
    db.run(askingDao.save(Asking(chatId, originalMessageId, ownerMessageId)))

}
