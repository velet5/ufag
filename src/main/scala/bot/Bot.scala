package bot

import bot.handler._
import telegram.{Telegram, Update}

import scala.concurrent.Future

trait Bot {
  def process(update: Update): Future[Outcome]
}

class BotImpl(telegram: Telegram,
              lingvoHandler: LingvoHandler,
              oxfordHandler: OxfordHandler,
              helpHandler: HelpHandler,
              ruDefineHandler: RuDefineHandler,
              startHandler: StartHandler,
              statisticsHandler: StatisticsHandler,
              askHandler: AskHandler,
              askReplyHandler: AskReplyHandler,
              subscribeHandler: SubscribeHandler,
              unsubscribeHandler: UnsubscribeHandler) extends Bot {

  def process(update: Update): Future[Outcome] = {
    val command = Command.parse(update)
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
      case subscribe: Subscribe => subscribeHandler.handle(subscribe)
      case unsubscribe: Unsubscribe => unsubscribeHandler.handle(unsubscribe)
      case malformed: Malformed => Future.successful(SendMessage(malformed.chatId, malformed.text))
      case Unknown(chatId) => Future.successful(SendMessage(chatId, "Неизвестная команда"))
      case CannotParse => Future.successful(CannotHandle)
    }
  }
}
