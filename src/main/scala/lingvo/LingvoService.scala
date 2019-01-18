package lingvo

import com.fasterxml.jackson.databind.ObjectMapper
import lingvo.LingvoProcessor.{EmptyResult, ProcessorError, ServiceError, UnknownResponse}
import persistence.model.Provider
import persistence.model.Provider.Provider
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
      .flatMap(process(word, _, Provider.Lingvo))
      .recover { case _ => Left("*Ошибка выполнения*") }

  def defineRu(word: String): Future[Either[String, String]] =
    lingvoClient
      .getRussianDefinition(word)
      .flatMap(process(word, _, Provider.LingvoRu))
      .recover {case _ => Left("*Ошибка выполнения*") }

  // private

  private def process(word: String, text: String, provider: Provider): Future[Either[String, String]] = {
    val requestType = provider match {
      case Provider.Lingvo => TranslationRequest
      case Provider.LingvoRu => DefinitionRequest
      case _ => throw new RuntimeException(s"wrong provider $provider")
    }

    processor.process(text).fold({
      case EmptyResult =>
        lingvoClient
          .getCorrections(word, requestType)
          .map(_.fold(emptyResult)(seq => formatCorrections(word, seq)))
      case other =>
        Future.successful(Left(formatError(other)))
    },
      s => {
        articleService.save(word, text, provider)
        Future.successful(Right(s))
      })
  }

  private def formatCorrections(word: String, seq: Seq[String]): Either[String, String] =
    Left("Возможно вы имели в виду: \n" +
      seq
        .sortBy(WordSimilarity.calculate(word, _))
        .take(5)
        .map(word => s"*$word*")
        .mkString("\n"))

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