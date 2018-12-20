import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import bot.handler._
import bot.{BotImpl, Commands}
import lingvo.{Authorizer, Lingvo, LingvoProcessor}
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

  val port: Int = properties.ufag.port

  val telegram = new TelegramImpl(properties.telegram.token, restClient)

  val queryDao = new QueryDao()(dbExecutionContext, AutoSession)
  val queryService = new QueryService(queryDao)

  val askingDao = new AskingDao()(dbExecutionContext, AutoSession)
  val askingService = new AskingService(askingDao)

  val articleDao = new ArticleDao()(dbExecutionContext, AutoSession)
  val articleService = new ArticleService(articleDao)

  val subscriptionDao = new SubscriptionDao()(dbExecutionContext, AutoSession)
  val subscriptionService = new SubscriptionService(subscriptionDao)

  val oxfordClient = new OxfordClient(properties.oxford, articleService, restClient, mapper)

  val db = new Db(properties.postgres)
  val authorizer = new Authorizer(properties.lingvo, restClient)
  val lingo = new Lingvo(authorizer, properties.lingvo, restClient, articleService, new LingvoProcessor)
  val ox = new OxfordServiceImpl(oxfordClient, new OxfordFormatter)

  val commands = new Commands(properties.ufag)

  val bot = new BotImpl(
    commands,
    telegram,
    new LingvoHandler(db, queryService, telegram, lingo),
    new OxfordHandler(ox),
    new HelpHandler,
    new RuDefineHandler(lingo),
    new StartHandler,
    new StatisticsHandler(queryService),
    new AskHandler(properties.ufag, telegram, askingService),
    new AskReplyHandler(askingService, telegram),
    new SubscribeHandler(subscriptionService),
    new UnsubscribeHandler(subscriptionService))

  val updateHandler = new UpdateHandlerImpl(telegram, bot)

}
