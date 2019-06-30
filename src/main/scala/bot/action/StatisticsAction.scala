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

class StatisticsAction[F[_] : Monad, Db[_]](
  telegramClient: TelegramClient[F],
  queryRepository: QueryRepository[Db],
  transact: Db ~> F,
) extends Action[F, Statistics] {

  def run(request: Request[Statistics]): F[Unit] =
    (queryRepository.count() ||> transact) >>= (send(request, _))

  // internal

  private def send(request: Request[Statistics], count: Int): F[Unit] =
    telegramClient
      .send(
        chatId = request.chatId,
        text = s"**Запросов**: $count"
      )
      .void

}
