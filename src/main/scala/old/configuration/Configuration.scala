package old.configuration

final case class Configuration(
  lingvo: LingvoProperties,
  oxford: OxfordProperties,
  ufag: UfagProperties,
  postgres: PostgresProperties,
  telegram: TelegramProperties,
  sentry: SentryProperties)

final case class SentryProperties(dsn: String)

final case class LingvoProperties(apiKey: String, serviceUrl: String)

final case class UfagProperties(port: Int, serviceUrl: String, ownerId: Long)

final case class PostgresProperties(
  connectionString: String,
  user: String,
  password: String,
  migrationsFile: String)

final case class TelegramProperties(token: String)

final case class OxfordProperties(
  appId: String,
  apiKey: String,
  serviceUrl: String)
