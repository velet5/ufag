package util.slick

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}
import slick.jdbc.PostgresProfile.api._

import slick.jdbc.JdbcType

object Mappers {

//  implicit val providerMapper: JdbcType[Provider] = MappedColumnType.base[Provider, Int](_.id, Provider.apply)

  implicit val zonedDateTimeMapper: JdbcType[ZonedDateTime] = MappedColumnType.base[ZonedDateTime, Timestamp](
    zdt => Timestamp.from(zdt.toInstant),
    ts => ts.toLocalDateTime.atZone(ZoneId.systemDefault())
  )

}
