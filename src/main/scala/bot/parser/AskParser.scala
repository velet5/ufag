package bot.parser

import bot.Parser
import model.bot.Command.Ask
import model.bot.Request
import model.telegram.Update

object AskParser extends Parser[Ask] {

  def parse(update: Update): Option[Request[Ask]] =
    ParserUtils
      .parseCommand(update)
      .map { case (message, _, textOpt) =>
        Request(
          chatId = message.chat.id,
          command = Ask(message.messageId, textOpt),
        )
      }

}
