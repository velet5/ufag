package persistence

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import org.slf4j.LoggerFactory

import scala.concurrent.Future


object Memory {

  case class PluralForms(`1`: String, `2-4`: String, `5-0`: String) {
    def format(number: Long): String = {
      val form =
        number.toString.last match {
          case '1' => `1`
          case '2' | '3' | '4' => `2-4`
          case _ => `5-0`
        }

      s"$number $form"
    }
  }

  val day = PluralForms("день", "дня", "дней")
  val hour = PluralForms("час", "часа", "часов")
  val minute = PluralForms("минуту", "минуты", "минут")
  val second = PluralForms("секунду", "секунды", "секунд")

  private def timeSince(time: ZonedDateTime): String = {
    val now = ZonedDateTime.now()
    val since = Seq(
      (ChronoUnit.DAYS.between(time, now), day),
      (ChronoUnit.HOURS.between(time, now) % 24, hour),
      (ChronoUnit.MINUTES.between(time, now) % 60, minute),
      (ChronoUnit.SECONDS.between(time, now) % 70, second))

    since
      .filter {case (number, _) => number > 0 }
      .take(2)
      .map {case (number, formatter) => formatter.format(number)}
      .mkString(" ")
  }
}

class Memory(db: Db) {

  import Memory._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = LoggerFactory.getLogger(getClass)

  def remember(chatId: Long, text: String, messageId: Long): Future[Unit] = {
    db.rememberQuery(chatId, text, ZonedDateTime.now(), messageId)
  }

  def recall(chatId: Long, text: String): Future[Option[Occurance]] = {
    db
      .get(chatId, text)
      .map(_.map(q => Occurance(q.text, q.time, q.messageId)))
      .recover {case ex => log.error("Recall failure", ex); None}
  }

  def fag(occurance: Occurance): String = {
    "Похоже что ты пидор, потому что я уже отвечал на этот запрос " + timeSince(occurance.when) + " назад"
  }


  def stat(): Future[Db.Stat] =
    db.fetchStat()

}


case class Occurance(text: String, when: ZonedDateTime, messageId: Long)


