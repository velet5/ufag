package bot.handler

import bot.{AskReply, Ignore, Outcome}
import persistence.model.Asking
import service.AskingService
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.Future

class AskReplyHandler(askingService: AskingService, telegram: Telegram) extends CommandHandler[AskReply] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle(command: AskReply): Future[Outcome] = {
    def send(asking: Asking) = {
      val message = TelegramSendMessage(command.userId, command.text, replyToMessageId = Some(asking.originalMessageId))

      telegram.sendMessage(message)
    }

    askingService
      .find(command.userId, command.replyMessageId)
      .collect { case Some(asking) => asking }
      .flatMap(send)
      .map(_ => Ignore)
  }
}
