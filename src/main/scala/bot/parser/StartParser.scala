package bot.parser

import bot.Parser
import bot.parser.ParserUtils._
import model.bot.Command.Start
import model.bot.Request
import model.telegram.Update

class StartParser extends Parser[Start] {

  override def parse(update: Update): Option[Request[Start]] =
    parseSimpleRequest(update, "/start", _ => Start)

}
