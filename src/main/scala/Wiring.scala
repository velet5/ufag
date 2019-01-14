import Application.{actorSystem, log, port, server, updateHandler}
import telegram.{TelegramImpl, UpdateHandlerImpl}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import bot.handler._
import bot.{BotImpl, Commands}
import http.Server
import lingvo.{LingvoClient, LingvoProcessor, LingvoService}
import oxford.{OxfordClient, OxfordFormatter, OxfordServiceImpl}
import persistence.Db
import persistence.dao.{ArticleDao, AskingDao, QueryDao}
import service.{ArticleService, AskingService, Monster, QueryService}

import scala.concurrent.ExecutionContext

trait Wiring extends Clients with Core {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  sys.addShutdownHook(() => {
    actorSystem.terminate()
  })

  val db = new Db(properties.postgres)

  val port: Int = properties.ufag.port

  val telegram = new TelegramImpl(properties.telegram.token, restClient, mapper)

  val queryDao = new QueryDao()
  val queryService = new QueryService(db, queryDao)

  val askingDao = new AskingDao()
  val askingService = new AskingService(db, askingDao)

  val articleDao = new ArticleDao()
  val articleService = new ArticleService(db, articleDao)

  val oxfordClient = new OxfordClient(properties.oxford, articleService, restClient, mapper)
  val ox = new OxfordServiceImpl(oxfordClient, new OxfordFormatter)

  val lingvoClient = new LingvoClient(properties.lingvo, articleService, restClient, mapper)
  val lingvo = new LingvoService(lingvoClient, new LingvoProcessor(mapper), articleService, mapper)

  val commands = new Commands(properties.ufag)

  val bot = new BotImpl(
    commands,
    telegram,
    new LingvoHandler(queryService, telegram, lingvo),
    new OxfordHandler(ox),
    new HelpHandler,
    new RuDefineHandler(lingvo),
    new StartHandler,
    new StatisticsHandler(queryService),
    new AskHandler(properties.ufag, telegram, askingService),
    new AskReplyHandler(askingService, telegram))

  val updateHandler = new UpdateHandlerImpl(telegram, bot)

  val server = new Server(port, updateHandler)

  val monster = new Monster(telegram, properties.ufag)

}
