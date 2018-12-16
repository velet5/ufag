package bot.handler

import bot._
import configuration.UfagProperties
import service.AskingService
import telegram.{Telegram, TelegramForwardMessage, TelegramResponse}

import scala.concurrent.{ExecutionContext, Future}

class AskHandler(properties: UfagProperties,
                 telegram: Telegram,
                 askingService: AskingService)
                (implicit executionContext: ExecutionContext) extends CommandHandler[Ask] {

  override def handle(command: Ask): Future[Outcome] = {
    val message = TelegramForwardMessage(
      chatId = properties.ownerId,
      fromChatId = command.chatId.value,
      command.messageId)

    def save(response: TelegramResponse) =
      askingService.save(command.chatId.value, command.messageId, response.result.messageId)

    telegram
      .forwardMessage(message)
      .flatMap(save)
      .map(_ => Ignore)
  }

}
