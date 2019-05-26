package wiring

import cats.effect.Sync
import cats.syntax.functor._
import telegram.TelegramUpdateHandler

case class TelegramModule[F[_]](
  telegramUpdateHandler: TelegramUpdateHandler[F]
)

object TelegramModule {

  def create[F[_]](implicit F: Sync[F]): F[TelegramModule[F]] =
    for {
      handler <- TelegramUpdateHandler.create
    } yield TelegramModule(handler)

}