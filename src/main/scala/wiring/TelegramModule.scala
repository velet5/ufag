package wiring

import cats.effect.Sync
import cats.syntax.functor._
import slick.dbio.DBIO
import telegram.TelegramClient

case class TelegramModule[F[_]](
  telegramClient: TelegramClient[F],
)

object TelegramModule {

  def create[F[_] : Sync](commonModule: CommonModule[F, DBIO]): F[TelegramModule[F]] =
    for {
      telegramClient <- TelegramClient.create[F](
        commonModule.configuration.telegram
      )(
        Sync[F], commonModule.sttpBackend
      )
    } yield TelegramModule(telegramClient)

}