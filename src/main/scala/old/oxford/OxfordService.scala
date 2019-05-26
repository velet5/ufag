package old.oxford

import org.slf4j.LoggerFactory
import old.oxford.exception.FailedRequestException

import scala.concurrent.{ExecutionContext, Future}

trait OxfordService {
  def define(rawText: String): Future[String]
}

class OxfordServiceImpl(oxfordClient: OxfordClient,
                        formatter: OxfordFormatter)
                       (implicit ex: ExecutionContext) extends OxfordService {

  def define(rawText: String): Future[String] = {
    oxfordClient.getDefinition(rawText)
      .map(formatter.format)
      .recover {
        case ex: FailedRequestException if ex.statusCode == 404 =>
          "*Слово не найдено*"

        case ex: Throwable =>
          log.error("Error processing old.oxford request", ex)
          "*Ошибка обработки*"
      }
  }

  // private

  private val log = LoggerFactory.getLogger(this.getClass)

}
