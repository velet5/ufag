package bot.handler

import bot._
import configuration.Configuration

import scala.concurrent.Future

private object AskHandler {

  private val ownerChatId = ChatId(Configuration.properties.ufag.ownerId)

}

class AskHandler extends CommandHandler[Ask] {

  import AskHandler._

  override def handle(command: Ask): Future[Outcome] =
    Future.successful(
      ForwardMessage(
        senderChatId = command.chatId,
        receiverChatId = ownerChatId,
        messageId = command.messageId))

}
