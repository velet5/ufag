package oxford

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import configuration.Configuration
import http.Client
import http.Client.Response
import org.apache.http.message.BasicHeader
import persistence.Db
import persistence.Db.Provider

import scala.concurrent.Future

object Oxford {
  private val url = "https://od-api.oxforddictionaries.com/api/v1"
  private val entriesApi = "/entries/en/"

  private val applicationId = Configuration.properties.oxford.appId
  private val key = Configuration.properties.oxford.apiKey
}


class Oxford(db: Db, client: Client) {

  import Oxford._
  private val processor = new OxfordProcessor

  import scala.concurrent.ExecutionContext.Implicits.global

  def define(rawText: String): Future[String] = {
    val appIdHeader = new BasicHeader("app_id", applicationId)
    val appKeyHeader = new BasicHeader("app_key", key)
    val text = URLEncoder.encode(rawText, StandardCharsets.UTF_8.name()).replace("\\+", "%20")
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
          "Processing error"
      }

  }

  private def processResponse(response: Response): Option[String] = {
    response.body.filter(_ => response.status == 200)
  }

  sealed trait Fetched
  case class FromDb(value: String) extends Fetched
  case class FromOx(option: Option[String]) extends Fetched


}
