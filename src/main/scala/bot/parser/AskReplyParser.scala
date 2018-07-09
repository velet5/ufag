package bot.parser

import bot.{AskReply, UpdateParser}
import configuration.Configuration
import telegram.Update

private object AskReplyParser {
  val ownerId: Long = Configuration.properties.ufag.ownerId
}

class AskReplyParser extends UpdateParser[AskReply] {
  import AskReplyParser._

  override def parse(update: Update): Option[AskReply] =
    for {
      message <- update.message if message.chat.id == ownerId
      reply <- message.replyToMessage
      user <- reply.forwardFrom
      text <- message.text
    } yield AskReply(user.id, reply.messageId, text)
}
