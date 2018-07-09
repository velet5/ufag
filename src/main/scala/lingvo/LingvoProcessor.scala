package lingvo

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.slf4j.LoggerFactory

import scala.util.Try

object LingvoProcessor {
  private val mapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_NULL)

  private val romanTen = Vector("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")
  private val romans: Vector[String] = romanTen ++ romanTen.map("X" + _) ++ romanTen.map("XX" + _)
}

class LingvoProcessor {

  import LingvoProcessor._

  private val log = LoggerFactory.getLogger(getClass)

  private val emptyResult = "*Ничего не найдено*"
  private val error = "*Ошибка работы сервиса*"
  private val unknown = "*Неизвестный ответ сервиса переводов*"

  def process(json: String): Either[String, String] = {
      if (json.startsWith("[")) {
        val tried: Try[Seq[ArticleModel]] =
          Try(mapper.readValue[Array[ArticleModel]](json, classOf[Array[ArticleModel]]))
        tried.failed.foreach(log.error("Error parsing lingvo json", _))
        tried.map(process).toOption.toRight(error)
      } else if (json.contains("No translations found")) {
        Left(emptyResult)
      } else {
        Left(unknown)
      }
  }

  def process(articleModels: Seq[ArticleModel]): String = {
    val alt: Option[Part] = articleModels.headOption.map(processAlt)
    val maybeBody = alt.map(printPart)
    val maybeTitle = articleModels.headOption.map("*" + _.title + "*\n")

    val result = for {
      title <- maybeTitle
      body <- maybeBody
    } yield title + body

    result.getOrElse(emptyResult)
  }

  def processString(model: ArticleModel): String = {
    implicit val sb: StringBuilder = new StringBuilder

    model.body.foreach(processNode)

    sb.toString()
  }

  def processAlt(model: ArticleModel): Part = {
    PartList(model.body.map(processAlt), 0)
  }

  private def processNode(node: ArticleNode)(implicit sb: StringBuilder): Unit = {
    if (node.isOptional) return
    node.node match {
      case NodeType.Paragraph =>
        processMarkup(node.markup)

      case NodeType.List =>
        node.items.foreach(processNode)

      case NodeType.Abbrev =>
        sb append "_" append node.text append "_ "

      case NodeType.Text | NodeType.CardRef =>
        if (node.node == NodeType.CardRef) sb.append("*")
        if (node.isItalics) {
          if (node.text.endsWith(" ")) {
            sb append "_" append node.text.substring(0, node.text.length - 1) append "_ "
          } else {
            sb append "_" append node.text append "_"
          }
        } else {
          sb append node.text
        }

        if (node.node == NodeType.CardRef) sb.append("* ")

      case NodeType.Transcription =>
        sb append "[" append node.text append "]\n"

      case NodeType.ListItem =>
        sb.append(" ")

      case NodeType.Comment =>
        node.markup.foreach(processNode)

      case _ =>
    }
  }

  sealed trait Part

  case class Abbreviation(value: String) extends Part
  case object Empty extends Part
  case class PartList(list: Seq[Part], tpe: Int) extends Part
  case class Paragraph(value: String) extends Part
  case class ListItem(parts: Seq[Part]) extends Part
  case class Comment(value: String) extends Part

  private def printPart(part: Part): String = {
    val sb = new StringBuilder

    def printPart0(part: Part, tpe: List[Int] = Nil, index: Int = 0): Unit = {
      def p[A](a: A): Unit = {
        sb.append(a)
      }
      def pln[A](a: A): Unit = {
        sb.append(a).append("\n")
      }

      part match {
        case Abbreviation(value) =>
          pln(value + " ")
        case PartList(list, t) =>
          list.zipWithIndex.foreach {case (p, i) =>
            printPart0(p, if (t == 0) tpe else t :: tpe, i)
          }
        case Paragraph(value) =>
          if (tpe.headOption.contains(4))
            p(value)
          else
            pln(value)
        case ListItem(parts) =>
          if (tpe.headOption.contains(1)) pln("*" + romans(index) + "*. ")
          if (tpe.headOption.contains(2)) p("*" + (index + 1) + ".* ")
          if (tpe.headOption.contains(3)) p ((index + 1) + ") ")
          if (tpe.headOption.contains(4)) p ("• ")
          parts.foreach(printPart0(_, tpe, if (parts.size > 1) index else 0))
          if (tpe.headOption.contains(4) || tpe.headOption.contains(1)) sb append "\n"

        case Comment(value) =>
          sb append "_(" append value append ")_"
          
        case Empty =>
      }
    }

    printPart0(part)

    sb.toString()
  }


  private def processAlt(node: ArticleNode): Part = {
    if (node.isOptional) return Empty
    node.node match {
      case NodeType.Paragraph =>
        val sb = new StringBuilder
        processMarkup(node.markup)(sb)
        Paragraph(sb.toString())

      case NodeType.List =>
        PartList(node.items.map(processAlt), node.`type`)

      case NodeType.Abbrev =>
        Abbreviation(node.text)

      case NodeType.ListItem =>
        ListItem(node.markup.map(processAlt))

      case NodeType.Comment =>
        Comment(node.text)

      case _ =>
        Empty
    }
  }

  private def processMarkup(nodes: Seq[ArticleNode])(implicit sb: StringBuilder): Unit = {
    val maybeTranscription = nodes.find(_.node == NodeType.Transcription)

    maybeTranscription match {
      case Some(transcription) =>
        sb append "\\[" append transcription.text append "]"
      case None =>
        nodes.foreach(processNode)
    }
  }

}

