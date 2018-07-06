package bot.handler

import bot.{Command, Outcome}

import scala.concurrent.Future

trait CommandHandler[C <: Command] {
  def handle(command: C): Future[Outcome]
}