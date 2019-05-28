package telegram

import cats.effect.Sync
import model.telegram.{Ok, Update}

class TelegramUpdateHandler[F[_]] {

  def handle(update: Update): F[Ok] = ???

}

object TelegramUpdateHandler {

  def create[F[_]](implicit F: Sync[F]): F[TelegramUpdateHandler[F]] =
    F.delay(new TelegramUpdateHandler[F])

}
