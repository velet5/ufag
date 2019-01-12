package service

import java.time.ZonedDateTime

import persistence.Db
import persistence.dao.QueryDao
import persistence.model.Query

import scala.concurrent.Future

class QueryService(db: Db, queryDao: QueryDao) {

  def save(chatId: Long, text: String, messageId: Long): Future[Int] =
    db.run(queryDao.save(Query(chatId, text, ZonedDateTime.now(), messageId)))

  def find(chatId: Long, text: String): Future[Option[Query]] =
    db.run(queryDao.find(chatId, text))

  def count(): Future[Int] =
    db.run(queryDao.count())

}
