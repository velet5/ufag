package oxford.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
case class OxfordResponse(results: Seq[Result])


@JsonIgnoreProperties(ignoreUnknown = true)
case class Result(id: String,
                  language: String,
                  word: String,
                  `type`: String,
                  lexicalEntries: Seq[LexicalEntry])


@JsonIgnoreProperties(ignoreUnknown = true)
case class LexicalEntry(entries: Seq[Entry])


@JsonIgnoreProperties(ignoreUnknown = true)
case class Entry(grammaticalFeatures: Seq[GrammaticalFeature],
                 senses: Seq[Sense])


@JsonIgnoreProperties(ignoreUnknown = true)
case class GrammaticalFeature(text: String, `type`: String)


@JsonIgnoreProperties(ignoreUnknown = true)
case class Sense(definitions: Option[Seq[String]],
                 shortDefinitions: Option[Seq[String]])