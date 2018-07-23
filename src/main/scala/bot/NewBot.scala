package bot

import bot.handler._
import http.Client
import persistence.{Db, Memory}
import telegram.{Telegram, Update}

import scala.concurrent.Future

object NewBot {

  private val client = new Client
  private val db = new Db
  private val memory = new Memory(db)
  private val ox = new oxford.Oxford(db, client)
  private val telegram = new Telegram(client)
  private val li = new lingvo.Lingvo(client, db)

  private val helpHandler = new HelpHandler
  private val startHandler = new StartHandler
  private val statisticsHandler = new StatisticsHandler(memory)
  private val oxfordHandler = new OxfordHandler(ox)
  private val askHandler = new AskHandler(telegram, db)
  private val askReplyHandler = new AskReplyHandler(db, telegram)
  private val lingvoHandler = new LingvoHandler(db, memory, telegram, li)
  private val ruDefineHandler = new RuDefineHandler(li)
  private val subscribeHandler = new SubscribeHandler(db)
  private val unsubscribeHandler = new UnsubscribeHandler(db)

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
