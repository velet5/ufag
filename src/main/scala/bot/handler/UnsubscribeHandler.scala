package bot.handler

import bot.{Outcome, SendMessage, Unsubscribe}
import org.slf4j.LoggerFactory
import service.SubscriptionService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class UnsubscribeHandler(subscriptionService: SubscriptionService)
                        (implicit ec: ExecutionContext) extends CommandHandler[Unsubscribe] {

  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Unsubscribe): Future[Outcome] = {
    val message = SendMessage(command.chatId, "Вы отписаны от получения новостей.")
    
    subscriptionService
      .unsubscribe(command.chatId.value)
      .map(_ => message)
      .recover { case NonFatal(ex) =>
        log.error(s"Can't unsubscribe chat id ${command.chatId.value}:", ex)
        message
      }
  }

}
