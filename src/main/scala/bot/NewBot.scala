package bot

import bot.handler.{HelpHandler, OxfordHandler, StartHandler, StatisticsHandler}
import http.Client
import persistence.{Db, Memory}
import telegram.Update

import scala.concurrent.Future


object NewBot {

  private val client = new Client
  private val db = new Db
  private val memory = new Memory(db)
  private val ox = new oxford.Oxford(db, client)

  private val helpHandler = new HelpHandler
  private val startHandler = new StartHandler
  private val statisticsHandler = new StatisticsHandler(memory)
  private val oxfordHandler = new OxfordHandler(ox)

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
      case start: Start => startHandler.handle(start)
      case statictics: Statistics => statisticsHandler.handle(statictics)
      case oxford: Oxford => oxfordHandler.handle(oxford)
      case _ => Future.successful(CannotHandle)
    }
  }
}
