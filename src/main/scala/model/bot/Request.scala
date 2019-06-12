package model.bot

import model.telegram.Update.ChatId

case class Request[C <: Command](chatId: ChatId, command: C)
