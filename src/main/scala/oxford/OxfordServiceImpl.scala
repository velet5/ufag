package oxford

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
      .recover { case _ => "*Ошибка обработки*" }
  }

}
