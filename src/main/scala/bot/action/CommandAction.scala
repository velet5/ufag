package bot.action

import bot.{Command, Outcome}

import scala.concurrent.Future

trait CommandAction[C <: Command] {
  def run(command: C): Future[Outcome]
}