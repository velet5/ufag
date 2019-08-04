package wiring

import bot.action.{AskAction, AskReplyAction, DefineEnAction, HelpAction, StatisticsAction}
import bot.parser.ParserUtils._
import bot.parser.{AskParser, AskReplyParser, DefineEnParser}
import bot.{Handler, UpdateHandler}
import cats.effect.Sync
import cats.~>
import conf.Configuration
import model.bot.Command._
import model.telegram.Chat
import cats.syntax.flatMap._
import cats.syntax.functor._

case class BotModule[F[_]](
  updateHandler: UpdateHandler[F]
)

object BotModule {

  def create[F[_] : Sync, Db[_]](
    transact: Db ~> F,
    telegramModule: TelegramModule[F],
    repositoryModule: RepositoryModule[Db],
    configuration: Configuration,
  ): F[BotModule[F]] = {
    val helpHandler = Handler.create[F, Help](
      parseSimpleRequest(_, "/help", _ => Help),
      new HelpAction[F, Help](telegramModule.telegramClient)
    )

    val startHandler = Handler.create[F, Start](
      parseSimpleRequest(_, "/start", _ => Start),
      new HelpAction[F, Start](telegramModule.telegramClient)
    )

    val statisticsHandler = Handler.create[F, Statistics](
      parseSimpleRequest(_, "/stat", _ => Statistics),
      new StatisticsAction[F, Db](
        telegramModule.telegramClient,
        repositoryModule.queryRepository,
        transact,
      )
    )

    val askHandler = Handler.create[F, Ask](
      AskParser,
      new AskAction(
        telegramModule.telegramClient,
        configuration.ufag,
        repositoryModule.askRepository,
        transact,
      )
    )

    val askReplyHandler = Handler.create[F, AskReply](
      new AskReplyParser(Chat.Id(configuration.ufag.ownerId)),
      new AskReplyAction(
        telegramModule.telegramClient,
        repositoryModule.askRepository,
        transact
      )
    )

    val defineEnHandler = Handler.create[F, DefineEn](
      DefineEnParser,
      new DefineEnAction,
    )

    for {
      updateHandler <- UpdateHandler.create(List(
        startHandler,
        helpHandler,
        statisticsHandler,
        askHandler,
        askReplyHandler,
        defineEnHandler,
      ))
    } yield BotModule(updateHandler)
  }

}
