package model.client

import io.circe.generic.JsonCodec
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec, JsonKey}
import model.client.OxfordDefinition._

@JsonCodec
case class OxfordDefinition(results: Seq[Result])

object OxfordDefinition {

  implicit val configuration: Configuration = Configuration.default

  @JsonCodec
  case class Result(
    id: String,
    language: String,
    word: String,
    `type`: String,
    lexicalEntries: Seq[LexicalEntry],
  )

  @ConfiguredJsonCodec
  case class LexicalEntry(
    @JsonKey("derivativeOf")
    derivatives: Option[Seq[Derivative]],
    language: String,
    lexicalCategory: String,
    pronunciations: Option[Seq[Pronunciation]],
    text: String,
    entries: Seq[Entry],
  )

  @JsonCodec
  case class Derivative(text: String)

  @JsonCodec
  case class Pronunciation(
    audioFile: String,
    dialects: Seq[String],
    phoneticNotation: String,
    phoneticSpelling: String,
  )

  @JsonCodec
  case class Entry(
    etymologies: Seq[String],
    grammaticalFeatures: Seq[GrammaticalFeature],
    homographNumber: String,
    senses: Seq[Sense],
  )

  @JsonCodec
  case class GrammaticalFeature(text: String, `type`: String)

  @ConfiguredJsonCodec
  case class Sense(
    definitions: Option[Seq[String]],
    @JsonKey("short_definitions")
    shortDefinitions: Option[Seq[String]],
    subsenses: Option[Seq[Sense]],
    crossReferences: Option[Seq[CrossReference]])

  @JsonCodec
  case class CrossReference(text: String, `type`: String)

}
