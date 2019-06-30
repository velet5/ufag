package oxford

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

@JsonIgnoreProperties(ignoreUnknown = true)
case class OxfordResponse(results: Seq[Result])

@JsonIgnoreProperties(ignoreUnknown = true)
case class Result(
  id: String,
  language: String,
  word: String,
  `type`: String,
  lexicalEntries: Seq[LexicalEntry])

@JsonIgnoreProperties(ignoreUnknown = true)
case class LexicalEntry(
  @JsonProperty("derivativeOf")
  derivatives: Option[Seq[Derivative]],
  language: String,
  lexicalCategory: LexicalCategory,
  pronunciations: Option[Seq[Pronunciation]],
  text: String,
  entries: Seq[Entry])

@JsonIgnoreProperties(ignoreUnknown = true)
case class LexicalCategory(
  id: String,
  text: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Derivative(text: String)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Pronunciation(
  audioFile: String,
  dialects: Seq[String],
  phoneticNotation: String,
  phoneticSpelling: String)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Entry(
  etymologies: Seq[String],
  grammaticalFeatures: Seq[GrammaticalFeature],
  homographNumber: String,
  senses: Seq[Sense])

@JsonIgnoreProperties(ignoreUnknown = true)
case class GrammaticalFeature(text: String, `type`: String)

@JsonIgnoreProperties(ignoreUnknown = true)
case class Sense(
  definitions: Option[Seq[String]],
  @JsonProperty("short_definitions")
  shortDefinitions: Option[Seq[String]],
  subsenses: Option[Seq[Sense]],
  crossReferences: Option[Seq[CrossReference]])

@JsonIgnoreProperties(ignoreUnknown = true)
case class CrossReference(text: String, `type`: String)
