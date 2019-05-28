package model.bot

import old.bot.ChatId

case class Request[C <: Command](chatId: ChatId, command: C)
