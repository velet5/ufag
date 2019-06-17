package wiring

import bot.action.{HelpAction, StatAction}
import bot.parser.ParserUtils.parseSimpleRequest
import bot.{Handler, UpdateHandler}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.~>
import model.bot.Command.{Help, Start, Statistics}

case class BotModule[F[_]](
  updateHandler: UpdateHandler[F]
)

object BotModule {

  def create[F[_] : Sync, Db[_]](
    transact: Db ~> F,
    telegramModule: TelegramModule[F],
    repositoryModule: RepositoryModule[Db]
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
        new StatAction[F, Db](
          telegramModule.telegramClient,
          repositoryModule.queryRepository,
          transact,
        )
      )
      updateHandler <- UpdateHandler.create(List(
        startHandler,
        helpHandler,
        statisticsHandler,
      ))
    } yield BotModule(updateHandler)

}
