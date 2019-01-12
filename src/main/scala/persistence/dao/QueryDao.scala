package persistence.dao

import java.time.ZonedDateTime

import persistence.Tables
import persistence.model.Query
import slick.dbio.Effect.{Read, Write}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class QueryDao()
              (implicit ec: ExecutionContext) {

  import persistence.model.zonedDateTimeMapper

  def save(query: Query): DBIOAction[Int, NoStream, Write] =
    Tables.queries += query

  def find(chatId: Long, text: String): DBIOAction[Option[Query], NoStream, Read] = {
    val thirtyDaysAgo = ZonedDateTime.now().minusDays(30)

    Tables.queries
      .filter(_.chatId === chatId.bind)
      .filter(_.text === text.bind)
      .filter(_.time > thirtyDaysAgo.bind)
      .sortBy(_.time.desc)
      .result
      .headOption
  }

  def count(): DBIOAction[Int, NoStream, Read] =
    Tables.queries
      .size
      .result

}
