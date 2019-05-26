package old.bot.action

import old.bot.{AskReply, Ignore, Outcome}
import old.persistence.model.Asking
import old.service.AskingService
import old.telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.{ExecutionContext, Future}

class AskReplyAction(
  askingService: AskingService,
  telegram: Telegram[Future]
)(
  implicit executionContext: ExecutionContext
) extends CommandAction[AskReply] {

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
