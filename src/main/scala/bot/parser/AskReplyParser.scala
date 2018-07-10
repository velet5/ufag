package bot.parser

import bot.{AskReply, Malformed, UpdateParser}
import configuration.Configuration
import telegram.Update

private object AskReplyParser {
  val ownerId: Long = Configuration.properties.ufag.ownerId
}

class AskReplyParser extends UpdateParser[AskReply] {
  import AskReplyParser._

  override def parse(update: Update): Option[Either[Malformed, AskReply]] =
    for {
      message <- update.message if message.chat.id == ownerId
      reply <- message.replyToMessage
      user <- reply.forwardFrom
      text <- message.text
    } yield Right(AskReply(user.id, reply.messageId, text))
}
