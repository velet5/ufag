package persistence

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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

  def fag(when: ZonedDateTime): String = {
    "Похоже что ты пидор, потому что я уже отвечал на этот запрос " + timeSince(when) + " назад"
  }
}

