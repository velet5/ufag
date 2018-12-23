package lingvo

import com.fasterxml.jackson.databind.ObjectMapper
import lingvo.LingvoProcessor.{EmptyResult, ProcessorError, ServiceError, UnknownResponse}
import service.ArticleService
import util.WordSimilarity

import scala.concurrent.{ExecutionContext, Future}

class LingvoService(
  lingvoClient: LingvoClient,
  processor: LingvoProcessor,
  articleService: ArticleService,
  mapper: ObjectMapper
)(
  implicit ec: ExecutionContext
) {

  import LingvoService._

  def translate(word: String): Future[Either[String, String]] =
    lingvoClient
      .getTranslation(word)
      .map(processor.process)
      .flatMap(_.fold({
          case EmptyResult =>
            lingvoClient
              .getCorrections(word)
              .map(_.fold(emptyResult)(seq => formatCorrections(word, seq)))
          case other =>
            Future.successful(Left(formatError(other)))
        },
        s => Future.successful(Right(s))))
    .recover {case _ => Left("*Ошибка выполнения*") }

  def defineRu(word: String): Future[Either[String, String]] =
    lingvoClient
      .getRussianDefinition(word)
      .map(Right(_))
      .recover {case _ => Left("*Ошибка выполнения*") }

  // private

  private def formatCorrections(word: String, seq: Seq[String]): Either[String, String] = {
    Left("Возможно вы имели в виду: \n" +
      seq
        .sortBy(WordSimilarity.calculate(word, _))
        .take(5)
        .map(word => s"*$word* (/_$word)")
        .mkString("\n"))
  }

  private def formatError(error: ProcessorError): String =
    error match {
      case EmptyResult => "Ничего не найдено"
      case ServiceError => "Ошибка сервиса"
      case UnknownResponse => "Неизвестный ответ"
    }

}

object LingvoService {

  private val emptyTranslation = "Я не знаю такого слова"
  private val emptyResult: Either[String, String] = Left(emptyTranslation)

}