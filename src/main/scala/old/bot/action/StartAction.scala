package old.bot.action

import old.bot.{Outcome, SendMessage, Start}

import scala.concurrent.Future

class StartAction extends CommandAction[Start] {
  override def run(command: Start): Future[Outcome] =
    Future.successful {
      SendMessage(command.chatId, HelpAction.message)
    }
}