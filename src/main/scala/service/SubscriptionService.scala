package service

import persistence.dao.SubscriptionDao

import scala.concurrent.Future

class SubscriptionService(subscriptionDao: SubscriptionDao) {

  def subscribe(chatId: Long): Future[Unit] = {
    subscriptionDao.subscribe(chatId)
  }

  def unsubscribe(chatId: Long): Future[Unit] = {
    subscriptionDao.unsubscribe(chatId)
  }

}
