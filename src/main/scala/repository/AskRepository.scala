package repository

import model.repository.{Asking, AskingTable}
import model.telegram.Message
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api.{Query => _, _}

trait AskRepository[F[_]] {

  def save(asking: Asking): F[Int]

  def finByOwnerMessageId(messageId: Message.Id): F[Option[Asking]]

}

  object AskRepository {

    def create: AskRepository[DBIO] =
      new Impl

    class Impl extends AskRepository[DBIO] with Repository[DBIO, AskingTable]  {

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
