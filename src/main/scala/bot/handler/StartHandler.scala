package bot.handler

import bot.{Outcome, SendMessage, Start}

import scala.concurrent.Future

class StartHandler extends CommandHandler[Start] {
  override def handle(command: Start): Future[Outcome] =
    Future.successful {
      SendMessage(command.chatId, HelpHandler.message)
    }
}