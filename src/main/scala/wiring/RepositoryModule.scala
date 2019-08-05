package wiring

import repository.{ArticleRepository, AskRepository, QueryRepository}
import slick.dbio.DBIO

case class RepositoryModule[F[_]](
  queryRepository: QueryRepository[F],
  askRepository: AskRepository[F],
  articleRepository: ArticleRepository[F],
)

object RepositoryModule {

  def create: RepositoryModule[DBIO] =
    RepositoryModule(
      QueryRepository.create,
      AskRepository.create,
      ArticleRepository.create
    )

}
