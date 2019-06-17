package wiring

import cats.effect.Sync
import cats.syntax.functor._
import repository.QueryRepository
import slick.dbio.DBIO

case class RepositoryModule[F[_]](
  queryRepository: QueryRepository[F],
)

object RepositoryModule {

  def create[F[_] : Sync]: F[RepositoryModule[DBIO]] =
    for {
      queryRepository <- QueryRepository.create
    } yield RepositoryModule(queryRepository)

}
