package wiring

import cats.MonadError
import cats.effect.{Concurrent, ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import client.{LingvoClient, OxfordClient, TelegramClient}
import slick.dbio.DBIO

case class ClientModule[F[_]](
  telegramClient: TelegramClient[F],
  oxfordClient: OxfordClient[F],
  lingvoClient: LingvoClient[F],
)

object ClientModule {

  def create[F[_] : Concurrent : ContextShift](
    commonModule: CommonModule[F, DBIO]
  ): F[ClientModule[F]] =
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

      lingvoClient <- LingvoClient.create(
        commonModule.configuration.lingvo,
      )(
        Concurrent[F], ContextShift[F], commonModule.sttpBackend
      )
    } yield ClientModule(
      telegramClient,
      oxfordClient,
      lingvoClient,
    )

}
