package bot.parser

import bot.Parser
import model.bot.Command.DefineEn
import model.bot.Request
import model.telegram.Update
import org.apache.commons.lang3.StringUtils.removeStart
import util.text.LangUtils.startsWithCyrillic

object DefineEnParser extends Parser[DefineEn] {

  override def parse(update: Update): Option[Request[DefineEn]] =
    for {
      chatId <- update.message.map(_.chat.id)
      word <- text(update)
    } yield Request(chatId, DefineEn(word))

  // internal

  private def text(update: Update): Option[String] =
    update.message
      .flatMap(_.text)
      .filterNot(_.startsWith("/"))
      .map(removeStart(_, "/"))
      .map(_.trim)
      .filterNot(startsWithCyrillic)

}
