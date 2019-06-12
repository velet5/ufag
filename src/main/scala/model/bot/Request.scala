package model.bot

import model.telegram.ChatId

case class Request[C <: Command](chatId: ChatId, command: C)
