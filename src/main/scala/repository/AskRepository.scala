package repository

import cats.effect.Sync
import model.repository.{Asking, AskingTable}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api.{Query => _, _}

trait AskRepository[F[_]] extends Repository[F, AskingTable] {

   def save(asking: Asking): F[Int]

}

object AskRepository {

  def create[F[_] : Sync]: F[AskRepository[DBIO]] =
    Sync[F].delay(new Impl)

  class Impl extends AskRepository[DBIO] {

    override def tableQuery = TableQuery[AskingTable]

    def save(asking: Asking): DBIO[Int] =
      tableQuery += asking
  }

}
