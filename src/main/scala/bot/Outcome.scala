package bot

sealed trait Outcome

final case class SendMessage(chatId: ChatId, text: String) extends Outcome

case object Ignore extends Outcome
case object CannotHandle extends Outcome
