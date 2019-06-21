package model.bot

import model.telegram.Message

sealed trait Command

object Command {

  type Start = Start.type
  type Help = Help.type
  type Statistics = Statistics.type

  case object Help extends Command
  case object Start extends Command
  case object Statistics extends Command
  case class Ask(messageId: Message.Id, text: Option[String]) extends Command

}
