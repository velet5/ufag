package bot.parser

import bot.{Command, Malformed}
import telegram.Update

trait CommandParser[C <: Command] {
  def parse(update: Update): Option[Either[Malformed, C]]
}
