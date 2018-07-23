package bot.handler

import bot.{Outcome, SendMessage, Subscribe}
import org.slf4j.LoggerFactory
import persistence.Db

import scala.concurrent.Future
import scala.util.control.NonFatal

class SubscribeHandler(db: Db) extends CommandHandler[Subscribe] {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Subscribe): Future[Outcome] = {
    val message = SendMessage(command.chatId, "Вы подписаны на новости")

    db
      .subscribe(command.chatId.value)
      .map(_ => message)
      .recover { case NonFatal(ex) =>
        log.error(s"Can't subscribe chat id ${command.chatId.value}", ex)
        message
      }
  }
  
}
