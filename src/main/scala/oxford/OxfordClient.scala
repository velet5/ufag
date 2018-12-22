package oxford

import client._
import com.fasterxml.jackson.databind.ObjectMapper
import configuration.OxfordProperties
import oxford.exception.{EmptyBodyException, FailedRequestException}
import persistence.model.Provider
import service.ArticleService
import util.UrlUtils

import scala.concurrent.{ExecutionContext, Future}

class OxfordClient(properties: OxfordProperties,
                   articleService: ArticleService,
                   restClient: RestClient,
                   mapper: ObjectMapper)
                  (implicit ec: ExecutionContext) {

  def getDefinition(word: String): Future[OxfordResponse] = {
    articleService
      .find(word, Provider.Oxford)
      .flatMap(_.fold(retrieve(word))(Future successful _.content))
      .map(mapper.readValue(_, classOf[OxfordResponse]))
  }

  // private

  private def retrieve(word: String): Future[String] = {
    val request = buildRequest(word)

    val handle = (response: Response) => {
      if (response.statusCode != 200) throw new FailedRequestException(request, response.statusCode)

      response.bodyOpt
        .getOrElse(throw new EmptyBodyException(request))
        .value
    }

    restClient.execute(request)
      .map(handle)
      .map { value =>
        articleService.save(word, value, Provider.Oxford) // we can do it in background
        value
      }
  }

  private def buildRequest(word: String): Request =
    Get(
      uri = Uri(properties.serviceUrl + "/entries/en/" + UrlUtils.encode(word)),
      headers = Seq(
        Header("app_id", properties.appId),
        Header("app_key", properties.apiKey)
      )
    )

}

