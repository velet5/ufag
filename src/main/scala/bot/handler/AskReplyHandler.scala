package bot.handler

import bot.{AskReply, Ignore, Outcome}
import persistence.Db
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.Future

class AskReplyHandler(db: Db, telegram: Telegram) extends CommandHandler[AskReply] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle(command: AskReply): Future[Outcome] = {
    db
      .getAsking(command.userId, command.replyMessageId)
      .collect { case Some(asking) => asking }
      .map {asking =>
         telegram.sendMessage(
           TelegramSendMessage(command.userId, command.text, replyToMessageId = Some(asking.originalMessageId)))
      }
      .map(_ => Ignore)
  }
}
