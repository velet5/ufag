package old.oxford

import old.util.RomanNumbers

class OxfordFormatter {

  def format(oxfordResponse: OxfordResponse): String = {
    oxfordResponse.results
      .headOption
      .map {result =>
        if (result.lexicalEntries.size > 1) {
          result.lexicalEntries
            .zipWithIndex.map {case (entry, index) => formatLexicalEntry(entry, Some(index + 1))}
        } else {
          result.lexicalEntries.map(formatLexicalEntry(_))
        }
      }
      .map(_.mkString("\n\n"))
      .getOrElse("")
  }

  // private

  private def formatLexicalEntry(lexicalEntry: LexicalEntry, index: Option[Int] = None): String = {
    val title = s"*${lexicalEntry.text}* (${lexicalEntry.lexicalCategory.toLowerCase})\n"
    val pronunciation = lexicalEntry.pronunciations.map { pronunciations =>
      pronunciations.map("*[" + _.phoneticSpelling + "]*").mkString("", " / ", "\n")
    }

    val entries = lexicalEntry.entries
    val entryStrings =
      if (entries.size > 1)
        entries.zipWithIndex.map { case (entry, i) => formatEntry(entry, Some(i + 1)) }
      else
        entries.map(formatEntry(_))

    val entriesText = entryStrings.filter(_.nonEmpty).mkString("\n\n")
    val article =
      if (entriesText.nonEmpty)
        entriesText
      else
        lexicalEntry.derivatives.toSeq.flatMap(_.map(d => s"see *${d.text}*")).mkString("", ", ", " ")

    title + pronunciation.getOrElse("") + article
  }

  private def formatEntry(entry: Entry, index: Option[Int] = None): String = {
    val indexStr = index.map(i => s"*${RomanNumbers(i)}.*\n").getOrElse("")
    val senses = entry.senses
      .zipWithIndex
      .map { case (sense, i) => formatSense(sense, index = Some(i + 1)) }
      .mkString("\n")

    indexStr + senses
  }

  private def formatSense(sense: Sense, index: Option[Int] = None, parentIndex: Option[Int] = None): String = {
    val indexStr = parentIndex.fold(index.map(i => s"$i. "))(parent => index.map(i => s"_$parent.${i})_ "))
    val maybeCrossReferences = sense.crossReferences.map(_.map(reference => s"_${reference.`type`}_ *${reference.text}* ").mkString(", "))

    val maybeDefinitions = sense.definitions.map {
      case Seq(single) =>
        single.mkString("")
      case several =>
        several.map("• " + _).mkString("\n")
    }.orElse {
      val references = sense.crossReferences.toSeq.flatMap(_.map(_.text))
      sense.shortDefinitions.map(_.filterNot(references.contains).mkString("\n"))
    }

    val maybeSubsenses = sense.subsenses.map {
      _.zipWithIndex
        .map { case (s, i) => formatSense(s, index = Some(i + 1), parentIndex = index) }
        .mkString("\n", "\n", "")
    }

    val parts = Seq(maybeCrossReferences, maybeDefinitions, maybeSubsenses).flatten

    parts match {
      case Seq() => ""
      case nonEmpty => indexStr.getOrElse("") + nonEmpty.mkString("")
    }
  }

}
