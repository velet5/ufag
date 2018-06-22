package oxford

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.slf4j.LoggerFactory
import oxford.model.OxfordResponse

import scala.util.Try


class OxfordProcessor {

  private val log = LoggerFactory.getLogger(getClass)

  private val mapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_NULL)

  def process(response: String): Either[String, String] = {
    Try(mapper.readValue(response, classOf[OxfordResponse])).fold(
      ex => {
        log.error("Can't parse oxford response", ex)
        Left("Ошибка")
      },
      ox => Right(format(ox)))
  }

  private def format(oxfordResponse: OxfordResponse): String = {
    val sb = new StringBuilder

    oxfordResponse.results.foreach {result =>
      sb append "*" append result.word append "*" append "\n"

      result.lexicalEntries.foreach {lexicalEntry =>
        lexicalEntry.entries.foreach {entry =>
          entry.senses.foreach {sense =>
            sense.definitions.foreach {definitions =>
              definitions.foreach { definition =>
                sb append "• " append definition append "\n"
              }
            }
          }
        }
      }
    }

    sb.toString()
  }

}
