package model.client

import io.circe.generic.JsonCodec
import LingvoArticle._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

import scala.collection.immutable

@JsonCodec
case class LingvoArticle(
  title: String,
  titleMarkup: List[Node],
  dictionary: String,
  articleId: String,
  body: List[Node],
)

object LingvoArticle {

  @JsonCodec
  case class Node(
    text: String,
    `type`: Int,
    items: List[Node],
    isItalics: Boolean,
    isAccent: Boolean,
    node: NodeType,
    fullText: String,
    fileName: String,
    dictionary: String,
    articleId: String,
    markup: List[Node],
    isOptional: Boolean,
  )

  sealed abstract class NodeType(
    val value: String
  ) extends StringEnumEntry

  object NodeType extends StringEnum[NodeType] with StringCirceEnum[NodeType] {

    case object Comment extends NodeType("Comment")

    case object Paragraph extends NodeType("Paragraph")

    case object Text extends NodeType("Text")

    case object List extends NodeType("List")

    case object ListItem extends NodeType("ListItem")

    case object Examples extends NodeType("Examples")

    case object ExampleItem extends NodeType("ExampleItem")

    case object Example extends NodeType("Example")

    case object CardRefs extends NodeType("CardRefs")

    case object CardRefItem extends NodeType("CardRefItem")

    case object CardRef extends NodeType("CardRef")

    case object Transcription extends NodeType("Transcription")

    case object Abbrev extends NodeType("Abbrev")

    case object Caption extends NodeType("Caption")

    case object Sound extends NodeType("Sound")

    case object Ref extends NodeType("Ref")

    case object Unsupported extends NodeType("Unsupported")

    override val values: immutable.IndexedSeq[NodeType] = findValues
  }


}