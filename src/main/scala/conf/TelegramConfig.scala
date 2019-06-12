package conf

import com.softwaremill.sttp.Uri
import conf.Configuration.TelegramProperties

case class TelegramConfig(
  sendMessageUri: Uri,
)

object TelegramConfig {

  def fromConfiguration(telegramProperties: TelegramProperties): TelegramConfig =
    TelegramConfig(
      sendMessageUri = uri(telegramProperties, "sendMessage")
    )

  // internal

  private def uri(telegramProperties: TelegramProperties, pathPart: String): Uri =
    Uri(
      scheme = "https",
      host = "api.telegram.org",
      port = 443,
      path = List(s"bot${telegramProperties.token}", pathPart)
    )
}
