package persistence.dao

import persistence.Tables
import persistence.model.Query
import slick.dbio.Effect.{Read, Write}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class QueryDao()
              (implicit ec: ExecutionContext) {

  def save(query: Query): DBIOAction[Int, NoStream, Write] =
    Tables.queries += query

  def find(chatId: Long, text: String): DBIOAction[Option[Query], NoStream, Read] =
    Tables.queries
      .filter(_.chatId === chatId.bind)
      .filter(_.text === text.bind)
      .result
      .headOption

  def count(): DBIOAction[Int, NoStream, Read] =
    Tables.queries
      .size
      .result

}
