package bot.parser

import bot.Parser
import model.bot.Command.Start
import model.bot.Request
import model.telegram.Update

class StartParser extends Parser[Start.type] {

  override def parse(update: Update): Option[Request[Start.type]] = ???

}
