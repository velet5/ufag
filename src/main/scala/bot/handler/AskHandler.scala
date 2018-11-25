package bot.handler

import bot._
import configuration.Configuration
import persistence.Db
import telegram.{Telegram, TelegramForwardMessage}

import scala.concurrent.{ExecutionContext, Future}

private object AskHandler {
  private val ownerChatId = ChatId(Configuration.properties.ufag.ownerId)
}

class AskHandler(telegram: Telegram, db: Db)
                (implicit executionContext: ExecutionContext) extends CommandHandler[Ask] {

  import AskHandler._

  override def handle(command: Ask): Future[Outcome] =
    telegram
      .forwardMessage(
        TelegramForwardMessage(
          chatId = ownerChatId.value,
          fromChatId = command.chatId.value,
          command.messageId))
     .flatMap(r => db.saveAsking(command.chatId.value, command.messageId, r.result.messageId))
     .map(_ => Ignore)
}
