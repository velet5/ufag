package repository

import java.time.ZonedDateTime

import cats.effect.Sync
import model.repository.{Query, QueryTable}
import slick.jdbc.PostgresProfile.api.{Query => _, _}
import util.slick.Mappers._

trait QueryRepository[F[_]] extends Repository[F, QueryTable] {
  def save(query: Query): F[Int]
  def find(chatId: Long, text: String): F[Option[Query]]
  def count(): F[Int]
}

object QueryRepository {

  def create[F[_] : Sync]: F[QueryRepository[DBIO]] =
    Sync[F].delay(new Impl())

  class Impl() extends QueryRepository[DBIO] {

    override def tableQuery = TableQuery[QueryTable]

    def save(query: Query): DBIO[Int] =
      tableQuery += query

    def find(chatId: Long, text: String): DBIO[Option[Query]] = {
      val thirtyDaysAgo = ZonedDateTime.now().minusDays(30)

      tableQuery
        .filter(_.chatId === chatId.bind)
        .filter(_.text === text.bind)
        .filter(_.time > thirtyDaysAgo.bind)
        .sortBy(_.time.desc)
        .result
        .headOption
    }

    def count(): DBIO[Int] =
      tableQuery
        .size
        .result

  }

}
