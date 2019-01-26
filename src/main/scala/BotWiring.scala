import bot._
import bot.action._
import bot.handler.UpdateHandler
import http.Server
import telegram.RequestHandlerImpl

trait BotWiring extends Components with Parsers {

  val bot = new BotImpl(
    telegram,
    Seq(
      ruDefineHandler,
      enDefineHandler,
      translationHandler,
      helpHandler,
      startHandler,
      statisticsHandler,
      askHandler,
      askReplyHandler
    )
  )

  val requestHandler = new RequestHandlerImpl(telegram, bot)
  val server = new Server(port, requestHandler)

  // private

  private lazy val ruDefineHandler = new UpdateHandler[RuDefine](
    ruDefineParser, new RuDefineAction(lingvo)
  )

  private lazy val enDefineHandler = new UpdateHandler[Oxford](
    enDefineHandler, new OxfordAction(ox)
  )

  private lazy val translationHandler = new UpdateHandler[Lingvo](
    translationParser, new LingvoAction(queryService, telegram, lingvo)
  )

  private lazy val helpHandler = new UpdateHandler[Help](
    helpParser, new HelpAction
  )

  private lazy val startHandler = new UpdateHandler[Start](
    startParser, new StartAction
  )

  private lazy val statisticsHandler = new UpdateHandler[Statistics](
    statParser, new StatisticsAction(queryService)
  )

  private lazy val askHandler = new UpdateHandler[Ask](
    askParser, new AskAction(properties.ufag, telegram, askingService)
  )

  private lazy val askReplyHandler = new UpdateHandler[AskReply](
    askReplyParser, new AskReplyAction(askingService, telegram)
  )

}
