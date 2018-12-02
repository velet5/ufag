import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import bot.BotImpl
import bot.handler._
import configuration.Configuration
import http.ClientImpl
import lingvo.Lingvo
import oxford.{OxfordProcessor, OxfordServiceImpl}
import persistence.{Db, Memory}
import telegram.{TelegramImpl, UpdateHandlerImpl}

import scala.concurrent.ExecutionContext

trait Wiring {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected val dbExecutionContext: ExecutionContext =
    ExecutionContext.fromExecutor(new ThreadPoolExecutor(4, 8, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue(100)))

  private val configuration = Configuration.properties

  val port: Int = configuration.ufag.port

  val client = new ClientImpl
  val telegram = new TelegramImpl(configuration.telegram.token, client)

  val db = new Db()(dbExecutionContext)
  val memory = new Memory(db)
  val lingo = new Lingvo(client, db)
  val ox = new OxfordServiceImpl(db, client, new OxfordProcessor)

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
