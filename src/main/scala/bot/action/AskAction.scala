package bot.action

import bot.Action
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, ~>}
import conf.Configuration.UfagProperties
import model.bot.Command.Ask
import model.bot.Request
import model.repository.Asking
import model.telegram.{Chat, Message}
import repository.AskRepository
import telegram.TelegramClient

class AskAction[F[_] : Monad, Db[_]](
  telegramClient: TelegramClient[F],
  ufagProperties: UfagProperties,
  askRepository: AskRepository[Db],
  transact: Db ~> F,
) extends Action[F, Ask] {

  import AskAction._

  override def run(request: Request[Ask]): F[Unit] =
    request.command.text match {
      case Some(_) =>
        (forward(request) >>= (save(request, _))) *> acknowledge(request)

      case None =>
        requireText(request)
    }

  // internal

  private def requireText(request: Request[Ask]): F[Unit] =
    telegramClient
      .send(request.chatId, EmptyTextMessage)
      .void

  private def forward(request: Request[Ask]): F[Message] =
    telegramClient
      .forward(
        from = request.chatId,
        to = Chat.Id(ufagProperties.ownerId),
        messageId = request.command.messageId,
      )

  private def save(request: Request[Ask], message: Message): F[Unit] =
    transact(
      askRepository.save(Asking(
        chatId = request.chatId,
        originalMessageId = request.command.messageId,
        ownerMessageId = message.messageId,
      ))
    ).void

  private def acknowledge(request: Request[Ask]): F[Unit] =
    telegramClient
      .send(
        chatId = request.chatId,
        message = "Сообщение доставлено!",
      )
      .void

}

object AskAction {

  val EmptyTextMessage = "Команда `/ask` требует текст. Напишите что-нибудь после /ask"

}
