package wiring

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import repository.{AskRepository, QueryRepository}
import slick.dbio.DBIO

case class RepositoryModule[F[_]](
  queryRepository: QueryRepository[F],
  askRepository: AskRepository[F],
)

object RepositoryModule {

  def create[F[_] : Sync]: F[RepositoryModule[DBIO]] =
    for {
      queryRepository <- QueryRepository.create
      askRepository <- AskRepository.create
    } yield RepositoryModule(queryRepository, askRepository)

}
