package oxford

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import configuration.Configuration
import http.Client
import http.Client.Response
import org.apache.http.message.BasicHeader
import org.slf4j.LoggerFactory
import persistence.Db
import persistence.Db.Provider

import scala.concurrent.Future

object OxfordService {
  private val url = "https://od-api.oxforddictionaries.com/api/v1"
  private val entriesApi = "/entries/en/"

  private val applicationId = Configuration.properties.oxford.appId
  private val key = Configuration.properties.oxford.apiKey

  sealed trait Fetched
  final case class FromDb(value: String) extends Fetched
  final case class FromOx(option: Option[String]) extends Fetched

}


class OxfordService(db: Db, client: Client) {

  import OxfordService._
  private val processor = new OxfordProcessor

  import scala.concurrent.ExecutionContext.Implicits.global

  def define(rawText: String): Future[String] = {
    val appIdHeader = new BasicHeader("app_id", applicationId)
    val appKeyHeader = new BasicHeader("app_key", key)
    val str = URLEncoder.encode(rawText.toLowerCase, StandardCharsets.UTF_8.name())
    val text = str.replace("+", "%20")
    val uri = url + entriesApi + text

    db
      .getArticle(text, Provider.Oxford)
      .flatMap {
        case Some(fromCache) =>
          Future.successful(FromDb(fromCache.content))
        case None =>
          client.get(uri, appIdHeader, appKeyHeader).map(processResponse).map(FromOx)
      }.map {
        case FromDb(value)  =>
          processor.process(value).fold(identity, identity)
        case FromOx(Some(value)) =>
          db.saveArticle(rawText, value, Provider.Oxford)
          processor.process(value).fold(identity, identity)
        case FromOx(None) =>
          "*Not found*"
      }
  }

  private def processResponse(response: Response): Option[String] =
    response.body.filter(_ => response.status == 200)

}
