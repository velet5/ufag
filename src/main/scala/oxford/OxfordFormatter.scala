package oxford

import util.RomanNumbers

class OxfordFormatter {

  def format(oxfordResponse: OxfordResponse): String = {
    implicit val sb: StringBuilder = new StringBuilder

    oxfordResponse.results.headOption.foreach {result =>
      if (result.lexicalEntries.size > 1) {
        result.lexicalEntries.zipWithIndex.foreach {case (entry, index) => printLexicalEntry(entry, Some(index + 1))}
      } else {
        result.lexicalEntries.headOption.foreach(printLexicalEntry(_))
      }
    }

    sb.toString()
  }

  // private

  private def printLexicalEntry(lexicalEntry: LexicalEntry, index: Option[Int] = None)
                               (implicit sb: StringBuilder): Unit = {
    sb append "*" append lexicalEntry.text append "* (" append lexicalEntry.lexicalCategory.toLowerCase append ") \n"
    lexicalEntry.pronunciations.foreach {pronunciations =>
      if (pronunciations.nonEmpty) {
        sb append pronunciations.map("*[" + _.phoneticSpelling + "]*").mkString(" / ") append "\n"
      }
    }

    val entries = lexicalEntry.entries
    if (entries.size > 1)
      entries.zipWithIndex.foreach { case (entry, i) => printEntry(entry, Some(i + 1)) }
    else
      entries.foreach(printEntry(_))

    sb.append("\n")
  }

  private def printEntry(entry: Entry, index: Option[Int] = None)
                        (implicit sb: StringBuilder): Unit = {
    index.foreach(i => sb append "*" append RomanNumbers(i) append ".*\n")
    entry.senses.zipWithIndex.foreach { case (sense, i) => printSense(sense, index = Some(i + 1)) }
  }

  private def printSense(sense: Sense, index: Option[Int] = None, parentIndex: Option[Int] = None)
                        (implicit sb: StringBuilder): Unit = {
    parentIndex match {
      case Some(parent) =>
        index.foreach(sb append "_" append parent append "." append _ append ")_ ")
      case None =>
        index.foreach(sb append _ append ". ")
    }

    sense.definitions.foreach { definitions =>
      if (definitions.size > 1)
        sense.definitions.zipWithIndex.foreach { case (definition, i) =>
          sb append "• " append definition append "\n"
        }
      else
        definitions.foreach(sb append _ append "\n")
    }

    sense.subsenses.foreach {senses =>
      senses.zipWithIndex.foreach { case (s, i) => printSense(s, index = Some(i + 1), parentIndex = index)}
    }
  }

}
