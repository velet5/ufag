package model.bot

import model.telegram.Chat

case class Request[C <: Command](chatId: Chat.Id, command: C)
