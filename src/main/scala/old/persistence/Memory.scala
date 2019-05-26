package old.persistence

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object Memory {

  def fag(when: ZonedDateTime): String = {
    val timeStr = timeSince(when).fold("только что")(_ + " назад")

    s"Похоже, что ты не выучил слово, потому что я уже отвечал на этот запрос $timeStr"
  }

  // private

  private case class PluralForms(`1`: String, `2-4`: String, `5-0`: String) {

    def format(number: Long): String = {
      val reminder = number % 10
      val form =
        if (number >= 5 && number <= 21) {
          `5-0`
        } else if (reminder >=2 && reminder <= 4) {
          `2-4`
        } else if (reminder == 1) {
          `1`
        } else {
          `5-0`
        }

      s"$number $form"
    }

  }

  private val day = PluralForms("день", "дня", "дней")
  private val hour = PluralForms("час", "часа", "часов")
  private val minute = PluralForms("минуту", "минуты", "минут")
  private val second = PluralForms("секунду", "секунды", "секунд")

  private def timeSince(time: ZonedDateTime): Option[String] = {
    val now = ZonedDateTime.now()
    val since = Seq(
      (ChronoUnit.DAYS.between(time, now), day),
      (ChronoUnit.HOURS.between(time, now) % 24, hour),
      (ChronoUnit.MINUTES.between(time, now) % 60, minute),
      (ChronoUnit.SECONDS.between(time, now) % 60, second))

    val str =
      since
        .filter {case (number, _) => number > 0 }
        .take(2)
        .map {case (number, formatter) => formatter.format(number)}
        .mkString(" ")

    Option(str).filter(_.nonEmpty)
  }

}

