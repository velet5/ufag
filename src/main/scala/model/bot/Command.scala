package model.bot

sealed trait Command

object Command {

  type Start = Start.type
  type Help = Help.type

  object Help extends Command
  object Start extends Command

}
