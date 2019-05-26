package old.bot.parser

import old.bot.{Command, Malformed}
import old.telegram.Update

trait CommandParser[C <: Command] {
  def parse(update: Update): Option[Either[Malformed, C]]
}
