package persistence

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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

    val notZero: Long => Boolean = _ > 0

    since
      .zip(since.tail)
      .find { case ((a, _), (b, _)) => notZero(a) && notZero(b) }
      .map { case (a, b) => Seq(a, b) }
      .orElse(since.find {case (a, _) => notZero(a)}.map(Seq(_)))
      .map(_.map { case (number, plural) => plural.format(number)}.mkString(" "))
      .getOrElse("только что")
  }
}

class Memory(db: Db) {

  import Memory._

  import scala.concurrent.ExecutionContext.Implicits.global

  def remember(chatId: Long, text: String, messageId: Long): Unit = {
    db.rememberQuery(chatId, text, ZonedDateTime.now(), messageId)
  }

  def recall(chatId: Long, text: String): Future[Option[Occurance]] = {
    db
      .get(chatId, text)
      .map(_.map(q => Occurance(q.text, q.time, q.messageId)))
      .recover {case ex => ex.printStackTrace(); None}
  }

  def fag(occurance: Occurance): String = {
    "Похоже что ты пидор, потому что я уже отвечал на этот запрос " + timeSince(occurance.when) + " назад"
  }


  def stat(): Future[Db.Stat] =
    db.fetchStat()

}


case class Occurance(text: String, when: ZonedDateTime, messageId: Long)


