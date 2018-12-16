package service

import persistence.dao.AskingDao
import persistence.model.Asking

import scala.concurrent.Future

class AskingService(askingDao: AskingDao) {

  def find(chatId: Long, ownerMessageId: Long): Future[Option[Asking]] = {
    askingDao.find(chatId, ownerMessageId)
  }

  def save(chatId: Long, originalMessageId: Long, ownerMessageId: Long): Future[Unit] = {
    askingDao.save(chatId, originalMessageId, ownerMessageId)
  }

}
