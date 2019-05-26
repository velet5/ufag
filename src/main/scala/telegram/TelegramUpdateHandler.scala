package telegram

import cats.effect.Sync
import model.telegram.{Ok, TelegramUpdate}

class TelegramUpdateHandler[F[_]] {

  def handle(update: TelegramUpdate): F[Ok] = ???

}

object TelegramUpdateHandler {

  def create[F[_]](implicit F: Sync[F]): F[TelegramUpdateHandler[F]] =
    F.delay(new TelegramUpdateHandler[F])

}
