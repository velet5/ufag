import bot.BotImpl
import bot.handler._
import configuration.Configuration
import http.Client
import lingvo.Lingvo
import oxford.OxfordService
import persistence.{Db, Memory}
import telegram.{TelegramImpl, UpdateHandlerImpl}

trait Wiring {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val configuration = Configuration.properties

  val client = new Client
  val telegram = new TelegramImpl(configuration.telegram.token, client)

  val db = new Db
  val memory = new Memory(db)
  val lingo = new Lingvo(client, db)
  val ox = new OxfordService(db, client)

  val bot = new BotImpl(
    telegram,
    new LingvoHandler(db, memory, telegram, lingo),
    new OxfordHandler(ox),
    new HelpHandler,
    new RuDefineHandler(lingo),
    new StartHandler,
    new StatisticsHandler(memory),
    new AskHandler(telegram, db),
    new AskReplyHandler(db, telegram),
    new SubscribeHandler(db),
    new UnsubscribeHandler(db))

  val updateHandler = new UpdateHandlerImpl(telegram, bot)

}
