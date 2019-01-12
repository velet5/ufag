package persistence.dao

import persistence.Tables
import persistence.model.Asking
import slick.dbio.Effect.{Read, Write}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class AskingDao()
               (implicit ec: ExecutionContext) {

  def find(chatId: Long, ownerMessageId: Long): DBIOAction[Option[Asking], NoStream, Read] =
    Tables.askings
      .filter(_.chatId === chatId.bind)
      .filter(_.ownerMessageId === ownerMessageId.bind)
      .result
      .headOption

  def save(asking: Asking): DBIOAction[Int, NoStream, Write] =
    Tables.askings += asking

}