package bot.action

import bot.Action
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{MonadError, ~>}
import client.TelegramClient
import model.bot.Command.AskReply
import model.bot.Request
import model.telegram.Message
import mouse.any._
import mouse.anyf._
import repository.AskRepository

import scala.util.control.NoStackTrace

class AskReplyAction[F[_] : MonadError[?[_], Throwable], Db[_]](
  telegramClient: TelegramClient[F],
  askRepository: AskRepository[Db],
  transact: Db ~> F,
) extends Action[F, AskReply] {

  import AskReplyAction._

  override def run(request: Request[AskReply]): F[Unit] =
    request.command.replyToMessageId.thrush(messageId =>
      askRepository
        .finByOwnerMessageId(messageId).||>(transact)
        .flatMap(_.liftTo(MessageNotFoundError(messageId)))
        .flatMap(asking =>
          telegramClient.reply(asking.chatId, asking.originalMessageId, request.command.text)
        )
        .void
    )
}

object AskReplyAction {

  case class MessageNotFoundError(messageId: Message.Id)
    extends RuntimeException(s"Message not found: ${messageId.value}")
      with NoStackTrace

}
