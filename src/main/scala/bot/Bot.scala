package bot

import bot.handler._
import telegram.{Telegram, Update}

import scala.concurrent.Future

trait Bot {
  def process(update: Update): Future[Outcome]
}

class BotImpl(commands: Commands,
              telegram: Telegram,
              lingvoHandler: CommandHandler[Lingvo],
              oxfordHandler: CommandHandler[Oxford],
              helpHandler: CommandHandler[Help],
              ruDefineHandler: CommandHandler[RuDefine],
              startHandler: CommandHandler[Start],
              statisticsHandler: CommandHandler[Statistics],
              askHandler: CommandHandler[Ask],
              askReplyHandler: CommandHandler[AskReply]) extends Bot {

  def process(update: Update): Future[Outcome] = {
    val command = commands.parse(update)
    val outcome = processCommand(command)

    outcome
  }

  // under the hood

  private def processCommand(command: Command): Future[Outcome] = {
    command match {
      case lingvo: Lingvo => lingvoHandler.handle(lingvo)
      case oxford: Oxford => oxfordHandler.handle(oxford)
      case help: Help => helpHandler.handle(help)
      case ruDefine: RuDefine => ruDefineHandler.handle(ruDefine)
      case start: Start => startHandler.handle(start)
      case statictics: Statistics => statisticsHandler.handle(statictics)
      case ask: Ask => askHandler.handle(ask)
      case askReply: AskReply => askReplyHandler.handle(askReply)
      case malformed: Malformed => Future.successful(SendMessage(malformed.chatId, malformed.text))
      case Unknown(chatId) => Future.successful(SendMessage(chatId, "Неизвестная команда"))
      case CannotParse => Future.successful(CannotHandle)
    }
  }
}
