package bot

import model.bot.{Command, Request}
import model.telegram.Update

trait Parser[C <: Command] {

  def parse(update: Update): Option[Request[C]]

}
