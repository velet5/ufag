package oxford

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

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
  derivatives: Seq[Any],
  language: String,
  lexicalCategory: String,
  pronunciations: Option[Seq[Pronunciation]],
  text: String,
  entries: Seq[Entry])

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
  shortDefinitions: Option[Seq[String]],
  subsenses: Option[Seq[Sense]])
