package bot.action

import bot.Action
import cats.Functor
import cats.syntax.functor._
import client.TelegramClient
import model.bot.{Command, Request}

class HelpAction[F[_] : Functor, C <: Command](telegramClient: TelegramClient[F]) extends Action[F, C] {

  override def run(request: Request[C]): F[Unit] =
    telegramClient
      .send(request.chatId, HelpAction.message)
      .void

}

object HelpAction {

  val message: String =
    s"""
       |Напишите слово для перевода, на русском или английском.
       |Например, _responsibility_ или _договор_ .
       |
       |Для получения толкования слова — добавьте перед словом знак вопроса:
       |_?luxury_ или _?бурбон_ .
       |
       |/ask - связаться с создателями бота.
       |/help - вывести это сообщение.
     """.stripMargin

}