package oxford

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import client.{Header, Response, RestClient, Uri}
import configuration.Configuration
import persistence.model.Provider
import service.ArticleService

import scala.concurrent.{ExecutionContext, Future}


trait OxfordService {
  def define(rawText: String): Future[String]
}

object OxfordServiceImpl {
  private val url = "https://od-api.oxforddictionaries.com/api/v1"
  private val entriesApi = "/entries/en/"

  private val applicationId = Configuration.properties.oxford.appId
  private val key = Configuration.properties.oxford.apiKey

  sealed trait Fetched
  final case class FromDb(value: String) extends Fetched
  final case class FromOx(option: Option[String]) extends Fetched

}

class OxfordServiceImpl(articleService: ArticleService, client: RestClient, processor: OxfordProcessor)
                       (implicit ex: ExecutionContext) extends OxfordService {

  import OxfordServiceImpl._

  def define(rawText: String): Future[String] = {
    val appIdHeader = Header("app_id", applicationId)
    val appKeyHeader = Header("app_key", key)
    val str = URLEncoder.encode(rawText.toLowerCase, StandardCharsets.UTF_8.name())
    val text = str.replace("+", "%20")
    val uri = url + entriesApi + text

    articleService
      .find(text, Provider.Oxford)
      .flatMap {
        case Some(fromCache) =>
          Future.successful(FromDb(fromCache.content))
        case None =>
          client.get(Uri(uri), Seq(appIdHeader, appKeyHeader)).map(processResponse).map(FromOx)
      }.map {
        case FromDb(value)  =>
          processor.process(value).fold(identity, identity)
        case FromOx(Some(value)) =>
          articleService.save(rawText, value, Provider.Oxford)
          processor.process(value).fold(identity, identity)
        case FromOx(None) =>
          "*Not found*"
      }
  }

  private def processResponse(response: Response): Option[String] =
    response.bodyOpt
      .filter(_ => response.statusCode == 200)
      .map(_.value)

}
