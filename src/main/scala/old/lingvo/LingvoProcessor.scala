package old.lingvo

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.slf4j.LoggerFactory
import old.util.RomanNumbers

import scala.util.Try

object LingvoProcessor {

  private val emptyResult = "*Ничего не найдено*"

  /** Part of the article */
  sealed trait Part

  /** Abbreviation */
  final case class Abbreviation(value: String) extends Part

  /** List of the parts */
  final case class PartList(list: Seq[Part], tpe: Int) extends Part

  /** Paragraph of an article */
  final case class Paragraph(value: String) extends Part

  /** Single item in some list */
  final case class ListItem(parts: Seq[Part]) extends Part

  /** Comment */
  final case class Comment(value: String) extends Part
  
  /** When we want to skip a part of article completely */
  case object Empty extends Part

  sealed trait ProcessorError
  case object EmptyResult extends ProcessorError
  case object ServiceError extends ProcessorError
  case object UnknownResponse extends ProcessorError
}

class LingvoProcessor(mapper: ObjectMapper) {

  import LingvoProcessor._

  def process(json: String): Either[ProcessorError, String] = {
      if (json.startsWith("[")) {
        val tried: Try[Seq[ArticleModel]] =
          Try(mapper.readValue[Array[ArticleModel]](json, classOf[Array[ArticleModel]]))
        tried.failed.foreach(log.error("Error parsing old.lingvo json", _))
        tried.map(processArticle).toOption.toRight(ServiceError)
      } else if (json.contains("No translations found")) {
        Left(EmptyResult)
      } else {
        Left(UnknownResponse)
      }
  }

  // under the hood

  private def processArticle(articleModels: Seq[ArticleModel]): String = {
    val maybeFirstModel = articleModels.headOption
    val maybeTitle = maybeFirstModel.map("*" + _.title + "*\n")
    val maybeBody = maybeFirstModel.map(convertModel).map(printPart)

    val result = for {
      title <- maybeTitle
      body <- maybeBody
    } yield title + body

    result.getOrElse(emptyResult)
  }

  private def convertModel(model: ArticleModel): Part =
    PartList(list = model.body.map(convertNode), tpe = 0)

  private def convertNode(node: ArticleNode): Part =
    node.node match {
      case _ if node.isOptional =>
        Empty
        
      case NodeType.Paragraph =>
        val sb = new StringBuilder
        collectText(node.markup)(sb)
        Paragraph(sb.toString())

      case NodeType.List =>
        PartList(node.items.map(convertNode), node.`type`)

      case NodeType.Abbrev =>
        Abbreviation(node.text)

      case NodeType.ListItem =>
        ListItem(node.markup.map(convertNode))

      case NodeType.Comment =>
        Comment(node.text)

      case _ =>
        Empty
    }

  private def collectText(node: ArticleNode)(implicit sb: StringBuilder): Unit = {
    if (node.isOptional) return
    node.node match {
      case NodeType.Paragraph =>
        collectText(node.markup)

      case NodeType.List =>
        node.items.foreach(collectText)

      case NodeType.Abbrev =>
        sb append "_" append node.text append "_ "

      case NodeType.Text | NodeType.CardRef =>
        if (node.node == NodeType.CardRef) sb.append("*")
        if (node.isItalics) {
          if (node.text.endsWith(" ")) {
            sb append "_" append node.text.trim append "_ "
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
        node.markup.foreach(collectText)

      case _ =>
        // ignore all others
    }
  }

  private def collectText(nodes: Seq[ArticleNode])(implicit sb: StringBuilder): Unit = {
    val maybeTranscription = nodes.find(_.node == NodeType.Transcription)

    maybeTranscription match {
      case Some(transcription) =>
        sb append "\\[" append transcription.text append "]"
      case None =>
        nodes.foreach(collectText)
    }
  }

  private def printPart(part: Part): String = {
    val sb = new StringBuilder

    def go(part: Part, tpe: List[Int] = Nil, index: Int = 0, parentIndex: Option[Int] = None): Unit = {
      def append[A](a: A): Unit = sb.append(a)
      def appendLine[A](a: A): Unit = sb.append(a).append("\n")

      part match {
        case Abbreviation(value) =>
          appendLine(value + " ")

        case PartList(list, t) =>
          list.zipWithIndex.foreach {case (p, i) =>
            go(p, if (t == 0) tpe else t :: tpe, i, Some(index).filter(_ => t > 0))
          }

        case Paragraph(value) =>
          if (tpe.headOption.contains(4))
            append(value)
          else
            appendLine(value)
          
        case ListItem(parts) =>
          if (tpe.headOption.contains(1)) appendLine("*" + RomanNumbers(index + 1) + "*. ")
          if (tpe.headOption.contains(2)) append("*" + (index + 1) + ".* ")
          if (tpe.headOption.contains(3)) {
            parentIndex
              .filter(_ => tpe.length > 1)
              .foreach(i => append((i + 1) + "."))
            
            append ((index + 1) + ") ")
          }
          if (tpe.headOption.contains(4)) append ("• ")
          parts.foreach(go(_, tpe, if (parts.size > 1) index else 0))
          if (tpe.headOption.contains(4) || tpe.headOption.contains(1)) sb append "\n"

        case Comment(value) =>
          sb append "_(" append value append ")_"
          
        case Empty =>
          //ignore
      }
    }

    go(part)

    sb.toString()
  }

  private val log = LoggerFactory.getLogger(getClass)

}
