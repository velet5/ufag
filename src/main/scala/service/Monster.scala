package service

import configuration.UfagProperties
import org.slf4j.LoggerFactory
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.{ExecutionContext, Future}

class Monster(
  telegram: Telegram, ufagProperties: UfagProperties
)(
  implicit ec: ExecutionContext
)
{

  def demonstrateSignsOfLiving(): Future[Unit] =
    telegram.sendMessage(TelegramSendMessage(
      chatId = ufagProperties.ownerId, text = "I'm alive"
    ))
    .recover { case ex =>
      log.error("Cannot live", ex)
    }
    .map(_ => ())

  // private

  private val log = LoggerFactory.getLogger(this.getClass)

}
