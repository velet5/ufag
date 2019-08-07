package conf

import java.time.ZoneId

import cats.effect.Sync
import conf.Configuration._
import pureconfig.module.catseffect.loadConfigF
import pureconfig.generic.auto._

final case class Configuration(
  zoneId: ZoneId,
  lingvo: LingvoProperties,
  oxford: OxfordProperties,
  ufag: UfagProperties,
  postgres: PostgresProperties,
  telegram: TelegramProperties,
  sentry: SentryProperties
)

object Configuration {

  def create[F[_]](implicit F: Sync[F]): F[Configuration] =
    loadConfigF("app")

  final case class SentryProperties(dsn: String)

  final case class LingvoProperties(apiKey: String, serviceUrl: String)

  final case class UfagProperties(port: Int, serviceUrl: String, ownerId: Long)

  final case class PostgresProperties(
    connectionString: String,
    user: String,
    password: String,
    migrationsFile: String
  )

  final case class TelegramProperties(token: String)

  final case class OxfordProperties(
    appId: String,
    apiKey: String,
    serviceUrl: String
  )

}