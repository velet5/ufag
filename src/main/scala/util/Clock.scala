package util

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.{Clock => CClock}
import cats.syntax.functor._

trait Clock[F[_]] {

  def instant: F[Instant]

  def zonedDateTime: F[ZonedDateTime]

}

object Clock {

  def create[F[_] : CClock : Functor](
    zoneId: ZoneId
  ): Clock[F] =
    new Impl(zoneId)

  private class Impl[F[_] : CClock : Functor](
    zoneId: ZoneId
  ) extends Clock[F] {

    override def instant: F[Instant] =
      CClock[F].realTime(TimeUnit.MILLISECONDS).map(Instant.ofEpochMilli)

    override def zonedDateTime: F[ZonedDateTime] =
      instant.map(_.atZone(zoneId))

  }


}
