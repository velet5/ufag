package wiring

import cats.Applicative
import cats.effect.Sync
import slick.dbio.DBIO

case class ServiceModule[F[_]]()

object ServiceModule {

  def create[F[_] : Sync](
    repositoryModule: RepositoryModule[DBIO],
  ): F[ServiceModule[F]] =
    Applicative[F].pure(ServiceModule())

}
