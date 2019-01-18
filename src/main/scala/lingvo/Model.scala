package lingvo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(classOf[UpperCamelCaseStrategy])
case class ArticleModel(title: String,
                        titleMarkup: Seq[ArticleNode],
                        dictionary: String,
                        articleId: String,
                        body: Seq[ArticleNode])

@JsonNaming(classOf[UpperCamelCaseStrategy])
@JsonIgnoreProperties(ignoreUnknown = true)
case class ArticleNode(text: String,
                       `type`: Int,
                       items: Seq[ArticleNode],
                       isItalics: Boolean,
                       isAccent: Boolean,
                       node: String,
                       fullText: String,
                       fileName: String,
                       dictionary: String,
                       articleId: String,
                       markup: Seq[ArticleNode],
                       isOptional: Boolean)

sealed trait RequestType
case object DefinitionRequest extends RequestType
case object TranslationRequest extends RequestType

object NodeType {
  val Comment = "Comment"
  val Paragraph = "Paragraph"
  val Text = "Text"
  val List = "List"
  val ListItem = "ListItem"
  val Examples = "Examples"
  val ExampleItem = "ExampleItem"
  val Example = "Example"
  val CardRefs = "CardRefs"
  val CardRefItem = "CardRefItem"
  val CardRef = "CardRef"
  val Transcription = "Transcription"
  val Abbrev = "Abbrev"
  val Caption = "Caption"
  val Sound = "Sound"
  val Ref = "Ref"
  val Unsupported = "Unsupported"
}
