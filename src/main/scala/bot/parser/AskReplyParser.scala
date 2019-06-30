package bot.parser

import bot.Parser
import model.bot.Command.AskReply
import model.bot.Request
import model.telegram.{Chat, Update}

class AskReplyParser(ownerChatId: Chat.Id) extends Parser[AskReply] {

  override def parse(update: Update): Option[Request[AskReply]] =
    for {
      message <- update.message if message.chat.id == ownerChatId
      replyToMessage <- message.replyToMessage
      text <- message.text
    } yield Request(
      chatId = message.chat.id,
      command = AskReply(
        replyToMessage.messageId,
        text
      ),
    )
}
