package wiring

import cats.MonadError
import cats.effect.Sync
import cats.syntax.functor._
import client.{OxfordClient, TelegramClient}
import slick.dbio.DBIO

case class ClientModule[F[_]](
  telegramClient: TelegramClient[F],
  oxfordClient: OxfordClient[F],
)

object ClientModule {

  def create[F[_] : Sync](commonModule: CommonModule[F, DBIO]): F[ClientModule[F]] =
    for {
      telegramClient <- TelegramClient.create[F](
        commonModule.configuration.telegram
      )(
        Sync[F], commonModule.sttpBackend
      )

      oxfordClient = OxfordClient.create[F](
        commonModule.configuration.oxford
      )(
        MonadError[F, Throwable],
        commonModule.sttpBackend,
      )
    } yield ClientModule(
      telegramClient,
      oxfordClient,
    )

}
