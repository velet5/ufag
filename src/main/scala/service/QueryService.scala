package service

import java.time.ZonedDateTime

import persistence.dao.QueryDao
import persistence.model.Query

import scala.concurrent.Future

class QueryService(queryDao: QueryDao) {

  def save(chatId: Long, text: String, messageId: Long): Future[Unit] = {
    queryDao.save(chatId, text, ZonedDateTime.now(), messageId)
  }

  def find(chatId: Long, text: String): Future[Option[Query]] = {
    queryDao.find(chatId, text)
  }

  def count(): Future[Int] = {
    queryDao.count()
  }

}
