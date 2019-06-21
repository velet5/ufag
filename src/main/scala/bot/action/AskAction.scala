package bot.action

import bot.Action
import cats.Functor
import model.bot.Command.Ask
import model.bot.Request
import telegram.TelegramClient
import cats.syntax.functor._
import conf.Configuration.TelegramProperties

class AskAction[F[_] : Functor](
  telegramClient: TelegramClient[F],
  telegramProperties: TelegramProperties,
) extends Action[F, Ask] {

  import AskAction._

  override def run(request: Request[Ask]): F[Unit] =
    request.command.text match {
      case Some(_) =>
         telegramClient
          .forward(request.chatId, ???, request.command.messageId)
          .void

      case None =>
        telegramClient
          .send(request.chatId, EmptyTextMessage)
          .void
    }

}

object AskAction {

  val EmptyTextMessage = "Команда `/ask` требует текст. Напишите что-нибудь после /ask"

}
