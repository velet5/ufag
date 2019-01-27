import bot._
import bot.action._
import bot.handler.UpdateHandler
import bot.parser.AskReplyParser
import bot.parser.Parsers._
import http.Server
import telegram.RequestHandlerImpl
import util.TextUtils.isCyrillic

import scala.concurrent.Future

trait BotWiring extends Components {

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
      askReplyHandler,
      unknownCommandHandler
    )
  )

  val requestHandler = new RequestHandlerImpl(telegram, bot)
  val server = new Server(port, requestHandler)

  /* private*/

  private lazy val ruDefineHandler = new UpdateHandler[RuDefine](
    definitionParser(_, isCyrillic, RuDefine),
    new RuDefineAction(lingvo)
  )

  private lazy val enDefineHandler = new UpdateHandler[Oxford](
    definitionParser(_, !isCyrillic(_), Oxford),
    new OxfordAction(ox)
  )

  private lazy val translationHandler = new UpdateHandler[Lingvo](
    defaultParser(Lingvo),
    new LingvoAction(queryService, telegram, lingvo)
  )

  private lazy val helpHandler = new UpdateHandler[Help](
    simpleCommandParser("/help", Help),
    new HelpAction
  )

  private lazy val startHandler = new UpdateHandler[Start](
    simpleCommandParser("/start", Start),
    new StartAction
  )

  private lazy val statisticsHandler = new UpdateHandler[Statistics](
    simpleCommandParser("/stat", Statistics),
    new StatisticsAction(queryService)
  )

  private lazy val askHandler = new UpdateHandler[Ask](
    withMessageId("/ask", Ask),
    new AskAction(properties.ufag, telegram, askingService)
  )

  private lazy val askReplyHandler = new UpdateHandler[AskReply](
    new AskReplyParser(properties.ufag),
    new AskReplyAction(askingService, telegram)
  )

  private lazy val unknownCommandHandler = new UpdateHandler[Unknown](
    unknownCommandParser,
    unknown => Future.successful(SendMessage(unknown.chatId, "Неизвестная команда"))
  )

}
