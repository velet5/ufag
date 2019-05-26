package old.bot.action

import old.bot.{Command, Outcome}

import scala.concurrent.Future

trait CommandAction[C <: Command] {
  def run(command: C): Future[Outcome]
}