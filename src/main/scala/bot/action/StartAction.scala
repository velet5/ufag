package bot.action

import bot.{Outcome, SendMessage, Start}

import scala.concurrent.Future

class StartAction extends CommandAction[Start] {
  override def run(command: Start): Future[Outcome] =
    Future.successful {
      SendMessage(command.chatId, HelpAction.message)
    }
}