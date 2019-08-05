package wiring

import bot.action._
import bot.parser.ParserUtils._
import bot.parser.{AskParser, AskReplyParser, DefineEnParser}
import bot.{Handler, UpdateHandler}
import cats.effect.Sync
import cats.syntax.functor._
import cats.~>
import conf.Configuration
import model.bot.Command._
import model.telegram.Chat

case class BotModule[F[_]](
  updateHandler: UpdateHandler[F]
)

object BotModule {

  def create[F[_] : Sync, Db[_]](
    transact: Db ~> F,
    clientModule: ClientModule[F],
    repositoryModule: RepositoryModule[Db],
    configuration: Configuration,
  ): F[BotModule[F]] = {
    val helpHandler = Handler.create[F, Help](
      parseSimpleRequest(_, "/help", _ => Help),
      new HelpAction[F, Help](clientModule.telegramClient)
    )

    val startHandler = Handler.create[F, Start](
      parseSimpleRequest(_, "/start", _ => Start),
      new HelpAction[F, Start](clientModule.telegramClient)
    )

    val statisticsHandler = Handler.create[F, Statistics](
      parseSimpleRequest(_, "/stat", _ => Statistics),
      new StatisticsAction[F, Db](
        clientModule.telegramClient,
        repositoryModule.queryRepository,
        transact,
      )
    )

    val askHandler = Handler.create[F, Ask](
      AskParser,
      new AskAction(
        clientModule.telegramClient,
        configuration.ufag,
        repositoryModule.askRepository,
        transact,
      )
    )

    val askReplyHandler = Handler.create[F, AskReply](
      new AskReplyParser(Chat.Id(configuration.ufag.ownerId)),
      new AskReplyAction(
        clientModule.telegramClient,
        repositoryModule.askRepository,
        transact
      )
    )

    val defineEnHandler = Handler.create[F, DefineEn](
      DefineEnParser,
      new DefineEnAction(
        repositoryModule.articleRepository,
        transact,
        clientModule.oxfordClient,
        clientModule.telegramClient,
      ),
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
