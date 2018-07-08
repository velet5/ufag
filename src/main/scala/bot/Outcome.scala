package bot

sealed trait Outcome

final case class SendMessage(chatId: ChatId, text: String) extends Outcome
final case class ForwardMessage(senderChatId: ChatId, receiverChatId: ChatId, messageId: Long) extends Outcome

case object Ignore extends Outcome
case object CannotHandle extends Outcome
