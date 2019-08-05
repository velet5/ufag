package repository

import java.time.ZonedDateTime

import cats.effect.Sync
import model.repository.{Query, QueryTable}
import model.telegram.Chat
import slick.jdbc.PostgresProfile.api.{Query => _, _}
import util.slick.Mappers._

trait QueryRepository[F[_]] {
  def save(query: Query): F[Int]
  def find(chatId: Chat.Id, text: String): F[Option[Query]]
  def count(): F[Int]
}

object QueryRepository {

  def create: QueryRepository[DBIO]=
    new Impl()

  private class Impl extends QueryRepository[DBIO] with Repository[DBIO, QueryTable]  {

    override def tableQuery = TableQuery[QueryTable]

    def save(query: Query): DBIO[Int] =
      tableQuery += query
    
    def find(chatId: Chat.Id, text: String): DBIO[Option[Query]] = {
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
