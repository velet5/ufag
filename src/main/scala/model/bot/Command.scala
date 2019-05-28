package model.bot

sealed trait Command

object Command {

  object Help extends Command
  object Start extends Command

}
