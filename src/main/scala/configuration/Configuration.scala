package configuration


final case class Configuration(
  lingvo: LingvoProperties,
  oxford: OxfordProperties,
  ufag: UfagProperties,
  postgres: PostgresProperties,
  telegram: TelegramProperties)


object Configuration {
  val properties: Configuration = pureconfig.loadConfigOrThrow[Configuration]
}


final case class LingvoProperties(apiKey: String, serviceUrl: String)
final case class UfagProperties(port: Int, serviceUrl: String, ownerId: Long)
final case class PostgresProperties(user: String, password: String)
final case class TelegramProperties(token: String)
final case class OxfordProperties(appId: String, apiKey: String)
