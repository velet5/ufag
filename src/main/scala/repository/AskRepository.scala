package repository

import cats.effect.Sync
import model.repository.{Asking, AskingTable}
import model.telegram.Message
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api.{Query => _, _}

trait AskRepository[F[_]] extends Repository[F, AskingTable] {

  def save(asking: Asking): F[Int]

  def finByOwnerMessageId(messageId: Message.Id): F[Option[Asking]]

}

  object AskRepository {

    def create[F[_] : Sync]: F[AskRepository[DBIO]] =
      Sync[F].delay(new Impl)

    class Impl extends AskRepository[DBIO] {

      override def tableQuery = TableQuery[AskingTable]

      def save(asking: Asking): DBIO[Int] =
        tableQuery += asking

      override def finByOwnerMessageId(messageId: Message.Id): DBIO[Option[Asking]] =
        tableQuery
          .filter(_.ownerMessageId === messageId.bind)
          .result
          .headOption
    }

  }
