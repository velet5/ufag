package bot.handler

import bot.{Outcome, SendMessage, Subscribe}
import org.slf4j.LoggerFactory
import service.SubscriptionService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SubscribeHandler(subscriptionService: SubscriptionService)
                      (implicit ec: ExecutionContext) extends CommandHandler[Subscribe] {

  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Subscribe): Future[Outcome] = {
    val message = SendMessage(command.chatId, "Вы подписаны на новости")

    subscriptionService
      .subscribe(command.chatId.value)
      .map(_ => message)
      .recover { case NonFatal(ex) =>
        log.error(s"Can't subscribe chat id ${command.chatId.value}", ex)
        message
      }
  }
  
}
