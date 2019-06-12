package wiring

import bot.Handler
import bot.action.HelpAction
import bot.parser.ParserUtils.parseSimpleRequest
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import model.bot.Command.{Help, Start}

case class BotModule[F[_]](
  handlers: List[Handler[F, _]],
)

object BotModule {

  def create[F[_] : Sync](telegramModule: TelegramModule[F]): F[BotModule[F]] =

    for {
      startHandler <- Handler.create[F, Start](
        parseSimpleRequest(_, "/start", _ => Start),
        new HelpAction[F, Start](telegramModule.telegramClient)
      )
      helpHandler <- Handler.create[F, Help](
        parseSimpleRequest(_, "/help", _ => Help),
        new HelpAction[F, Help](telegramModule.telegramClient)
      )
    } yield BotModule(
      List(
        startHandler,
        helpHandler,
      )
    )

}
