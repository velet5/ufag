package bot.parser

import cats.instances.option._
import cats.syntax.flatMap._
import model.bot.{Command, Request}
import model.telegram.Update
import model.telegram.Update.{Chat, Message}
import mouse.any._
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.startsWith

object ParserUtils {

  type CommandName = String
  type CommandText = String

  def parseCommand(update: Update): Option[(Chat, CommandName, Option[CommandText])] = {
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

    def parse(message: Message) =
      message.text
        .flatMap(_ |> split |> extract)
        .map { case (name, text) => (message.chat, name, text) }

    update.message >>= parse
  }

  def parseSimpleRequest[C <: Command](
    update: Update,
    name: String,
    constructor: Update => C,
  ): Option[Request[C]] =
    parseCommand(update)
      .filter { case (_, n, _) => n == name }
      .map { case (chat, _, _) => Request(chat.id, constructor(update)) }

}
