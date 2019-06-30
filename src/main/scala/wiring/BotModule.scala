package wiring

import bot.action.{AskAction, AskReplyAction, HelpAction, StatisticsAction}
import bot.parser.{AskParser, AskReplyParser}
import bot.parser.ParserUtils._
import bot.{Handler, UpdateHandler}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.~>
import conf.Configuration
import model.bot.Command.{Ask, AskReply, Help, Start, Statistics}
import model.telegram.Chat

case class BotModule[F[_]](
  updateHandler: UpdateHandler[F]
)

object BotModule {

  def create[F[_] : Sync, Db[_]](
    transact: Db ~> F,
    telegramModule: TelegramModule[F],
    repositoryModule: RepositoryModule[Db],
    configuration: Configuration,
  ): F[BotModule[F]] =
    for {
      startHandler <- Handler.create[F, Start](
        parseSimpleRequest(_, "/start", _ => Start),
        new HelpAction[F, Start](telegramModule.telegramClient)
      )
      helpHandler <- Handler.create[F, Help](
        parseSimpleRequest(_, "/help", _ => Help),
        new HelpAction[F, Help](telegramModule.telegramClient)
      )
      statisticsHandler <- Handler.create[F, Statistics](
        parseSimpleRequest(_, "/stat", _ => Statistics),
        new StatisticsAction[F, Db](
          telegramModule.telegramClient,
          repositoryModule.queryRepository,
          transact,
        )
      )
      askHandler <- Handler.create[F, Ask](
        AskParser,
        new AskAction(
          telegramModule.telegramClient,
          configuration.ufag,
          repositoryModule.askRepository,
          transact,
        )
      )
      askReplyHandler <- Handler.create[F, AskReply](
        new AskReplyParser(Chat.Id(configuration.ufag.ownerId)),
        new AskReplyAction(
          telegramModule.telegramClient,
          repositoryModule.askRepository,
          transact
        )
      )
      updateHandler <- UpdateHandler.create(List(
        startHandler,
        helpHandler,
        statisticsHandler,
        askHandler,
        askReplyHandler,
      ))
    } yield BotModule(updateHandler)

}
