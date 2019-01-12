package persistence

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import persistence.model.Provider.Provider
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

package object model {

  implicit val providerMapper: JdbcType[Provider] = MappedColumnType.base[Provider, Int](_.id, Provider.apply)

  implicit val zonedDateTimeMapper: JdbcType[ZonedDateTime] = MappedColumnType.base[ZonedDateTime, Timestamp](
    zdt => Timestamp.from(zdt.toInstant),
    ts => ts.toLocalDateTime.atZone(ZoneId.systemDefault())
  )

}
