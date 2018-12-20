package bot.parser

import bot.{AskReply, Malformed, UpdateParser}
import configuration.UfagProperties
import telegram.Update

class AskReplyParser(properties: UfagProperties) extends UpdateParser[AskReply] {
  override def parse(update: Update): Option[Either[Malformed, AskReply]] =
    for {
      message <- update.message if message.chat.id == properties.ownerId
      reply <- message.replyToMessage
      user <- reply.forwardFrom
      text <- message.text
    } yield Right(AskReply(user.id, reply.messageId, text))
}
