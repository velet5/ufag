package old.bot

sealed trait Command

final case class Help(chatId: ChatId) extends Command
final case class Start(chatId: ChatId) extends Command
final case class Statistics(chatId: ChatId) extends Command
final case class Oxford(chatId: ChatId, word: String) extends Command
final case class Lingvo(chatId: ChatId, word: String) extends Command
final case class Ask(chatId: ChatId, messageId: Long) extends Command
final case class AskReply(userId: Long, replyMessageId: Long, text: String) extends Command
final case class RuDefine(chatId: ChatId, word: String) extends Command
final case class Malformed(chatId: ChatId, text: String) extends Command
final case class Unknown(chatId: ChatId) extends Command

case object CannotParse extends Command
