package lingvo

import com.fasterxml.jackson.databind.ObjectMapper
import service.ArticleService

import scala.concurrent.ExecutionContext

class LingvoService(
  lingvoClient: LingvoClient,
  processor: LingvoProcessor,
  articleService: ArticleService,
  mapper: ObjectMapper
)(
  implicit ec: ExecutionContext
) {

  def translate(word: String): 

}

object LingvoService {

  private val emptyTranslation = "Я не знаю такого слова"
  private val emptyResult: Either[String, String] = Left(emptyTranslation)

}