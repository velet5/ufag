package bot.action

import bot.Action
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import model.bot.Command.Statistics
import model.bot.Request
import mouse.anyf._
import repository.QueryRepository
import telegram.TelegramClient

class StatAction[F[_] : Monad, Db[_]](
  telegramClient: TelegramClient[F],
  queryRepository: QueryRepository[Db],
  transact: Db ~> F,
) extends Action[F, Statistics] {

  def run(request: Request[Statistics]): F[Unit] =
    queryRepository.count() ||> transact >>= (count =>
      telegramClient
        .send(request.chatId, s"**Запросов**: $count")
        .void
      )

}
