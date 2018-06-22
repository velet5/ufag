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

//
//object NodeType {
//  val Comment = 0
//  val Paragraph = 1
//  val Text = 2
//  val List = 3
//  val ListItem = 4
//  val Examples = 5
//  val ExampleItem = 6
//  val Example = 7
//  val CardRefs = 8
//  val CardRefItem = 9
//  val CardRef = 10
//  val Transcription = 11
//  val Abbrev = 12
//  val Caption = 13
//  val Sound = 14
//  val Ref = 15
//  val Unsupported = 16
//}

