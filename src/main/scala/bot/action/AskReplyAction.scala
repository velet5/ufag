package bot.action

import bot.{AskReply, Ignore, Outcome}
import persistence.model.Asking
import service.AskingService
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.Future

class AskReplyAction(askingService: AskingService, telegram: Telegram) extends CommandAction[AskReply] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def run(command: AskReply): Future[Outcome] = {
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
