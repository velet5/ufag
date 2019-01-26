package bot.action

import bot._
import configuration.UfagProperties
import service.AskingService
import telegram.{Telegram, TelegramForwardMessage, TelegramResponse}

import scala.concurrent.{ExecutionContext, Future}

class AskAction(properties: UfagProperties,
                 telegram: Telegram,
                 askingService: AskingService)
                (implicit executionContext: ExecutionContext) extends CommandAction[Ask] {

  override def run(command: Ask): Future[Outcome] = {
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
