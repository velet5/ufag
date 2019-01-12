import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import bot.handler._
import bot.{BotImpl, Commands}
import lingvo.{LingvoClient, LingvoProcessor, LingvoService}
import oxford.{OxfordClient, OxfordFormatter, OxfordServiceImpl}
import persistence.Db
import persistence.dao.{ArticleDao, AskingDao, QueryDao, SubscriptionDao}
import scalikejdbc.AutoSession
import service.{ArticleService, AskingService, QueryService, SubscriptionService}
import telegram.{TelegramImpl, UpdateHandlerImpl}

import scala.concurrent.ExecutionContext

trait Wiring extends Clients with Core {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected val dbExecutionContext: ExecutionContext =
    ExecutionContext.fromExecutor(new ThreadPoolExecutor(4, 8, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue(100)))

  val db = new Db(properties.postgres)

  locally {
    db.init()
  }

  val port: Int = properties.ufag.port

  val telegram = new TelegramImpl(properties.telegram.token, restClient, mapper)

  val queryDao = new QueryDao()(dbExecutionContext, AutoSession)
  val queryService = new QueryService(queryDao)

  val askingDao = new AskingDao()(dbExecutionContext, AutoSession)
  val askingService = new AskingService(askingDao)

  val articleDao = new ArticleDao()(dbExecutionContext)
  val articleService = new ArticleService(db, articleDao)

  val subscriptionDao = new SubscriptionDao()(dbExecutionContext, AutoSession)
  val subscriptionService = new SubscriptionService(subscriptionDao)

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
    new AskReplyHandler(askingService, telegram),
    new SubscribeHandler(subscriptionService),
    new UnsubscribeHandler(subscriptionService))

  val updateHandler = new UpdateHandlerImpl(telegram, bot)

}
