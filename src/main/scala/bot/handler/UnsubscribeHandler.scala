package bot.handler

import bot.{Outcome, SendMessage, Unsubscribe}
import org.slf4j.LoggerFactory
import persistence.Db

import scala.concurrent.Future
import scala.util.control.NonFatal

class UnsubscribeHandler(db: Db) extends CommandHandler[Unsubscribe] {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Unsubscribe): Future[Outcome] = {
    val message = SendMessage(command.chatId, "Вы отписаны от получения новостей.")
    
    db
      .unsubscribe(command.chatId.value)
      .map(_ => message)
      .recover { case NonFatal(ex) =>
        log.error(s"Can't unsubscribe chat id ${command.chatId.value}:", ex)
        message
      }
  }

}
