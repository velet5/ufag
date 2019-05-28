package bot.parser

import model.telegram.Update
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.startsWith

object ParserUtils {

  type CommandName = String
  type CommandText = String

  def parseCommand(update: Update): Option[(CommandName, Option[CommandText])] = {
    def split(text: String) =
        StringUtils
          .split(text, " ", 2)
          .toList
          .map(StringUtils.trim)
          .lift

    def extract(fn: Int => Option[String]) =
      fn(0)
        .filter(startsWith(_, "/"))
        .map((_, fn(1)))

//    update.
//      message
//      .flatMap(_.text)
//      .map(split)
//      .flatMap(extract)

    ???
  }

}
