package old.bot.action

import old.bot._
import old.configuration.UfagProperties
import old.service.AskingService
import old.telegram.{Telegram, TelegramForwardMessage, TelegramResponse}

import scala.concurrent.{ExecutionContext, Future}

class AskAction(properties: UfagProperties,
                 telegram: Telegram[Future],
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
