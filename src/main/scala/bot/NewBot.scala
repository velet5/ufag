package bot

import bot.handler.HelpHandler
import telegram.Update

import scala.concurrent.Future


object NewBot {

  private val helpHandler = new HelpHandler

}


class NewBot {
  import NewBot._

  def process(update: Update): Future[Outcome] = {
    val command = Command.parse(update)
    val outcome = processCommand(command)

    outcome
  }

  def processCommand(command: Command): Future[Outcome] = {
    command match {
      case help: Help => helpHandler.handle(help)
      case _ => Future.successful(CannotHandle)
    }
  }
}
