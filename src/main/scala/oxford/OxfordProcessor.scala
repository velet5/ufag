package oxford

import java.io.{PrintWriter, StringWriter}

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import oxford.model.OxfordResponse

import scala.io.Source
import scala.util.Try


object OxfordProcessor {
//
//  def main(args: Array[String]): Unit = {
//    val words = Seq(
//      Source.fromFile("/home/oarshinskii/get-up.json").mkString,
//      Source.fromFile("/home/oarshinskii/get.json").mkString)
//
//    val processor = new OxfordProcessor
//
//    words.foreach {word =>
//      val result = processor.process(word)
//
//      result.fold(left => println("LEFT: " + left), println)
//    }
//  }
}

class OxfordProcessor {

  private val mapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_NULL)

  def process(response: String): Either[String, String] = {
    Try(mapper.readValue(response, classOf[OxfordResponse])).fold(
      ex => {
        val writer = new StringWriter()
        ex.printStackTrace(new PrintWriter(writer, true))
        val string = writer.getBuffer.toString

        Left(s"```\n$string\n```")
      },
      ox => Right(format(ox)))
  }

  private def format(oxfordResponse: OxfordResponse): String = {
    val sb = new StringBuilder

    oxfordResponse.results.foreach {result =>
      sb append "*" append result.word append "*" append "\n"
      println("" + "*" + result.word + "*")
      result.lexicalEntries.foreach {lexicalEntry =>
        lexicalEntry.entries.foreach {entry =>
          entry.senses.foreach {sense =>
            sense.definitions.foreach {definitions =>
              definitions.foreach { definition =>
                sb append "• " append definition append "\n"
                println("• " + definition)
              }
            }
          }
        }
      }
    }

    sb.toString()
  }

}
