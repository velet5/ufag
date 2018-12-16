import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import bot.BotImpl
import bot.handler._
import configuration.Configuration
import http.ClientImpl
import lingvo.Lingvo
import oxford.{OxfordProcessor, OxfordServiceImpl}
import persistence.Db
import persistence.dao.{ArticleDao, AskingDao, QueryDao, SubscriptionDao}
import scalikejdbc.AutoSession
import service.{ArticleService, AskingService, QueryService, SubscriptionService}
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


  val queryDao = new QueryDao()(dbExecutionContext, AutoSession)
  val queryService = new QueryService(queryDao)

  val askingDao = new AskingDao()(dbExecutionContext, AutoSession)
  val askingService = new AskingService(askingDao)

  val articleDao = new ArticleDao()(dbExecutionContext, AutoSession)
  val articleService = new ArticleService(articleDao)

  val subscriptionDao = new SubscriptionDao()(dbExecutionContext, AutoSession)
  val subscriptionService = new SubscriptionService(subscriptionDao)

  val db = new Db(configuration.postgres)
  val lingo = new Lingvo(client, articleService)
  val ox = new OxfordServiceImpl(articleService, client, new OxfordProcessor)

  val bot = new BotImpl(
    telegram,
    new LingvoHandler(db, queryService, telegram, lingo),
    new OxfordHandler(ox),
    new HelpHandler,
    new RuDefineHandler(lingo),
    new StartHandler,
    new StatisticsHandler(queryService),
    new AskHandler(configuration.ufag, telegram, askingService),
    new AskReplyHandler(askingService, telegram),
    new SubscribeHandler(subscriptionService),
    new UnsubscribeHandler(subscriptionService))

  val updateHandler = new UpdateHandlerImpl(telegram, bot)

}
