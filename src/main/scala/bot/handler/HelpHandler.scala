package bot.handler

import bot.{Help, Outcome, SendMessage}

import scala.concurrent.Future

private object HelpHandler {
  val message: String =
    s"""
       |Напишите слово для перевода, на русском или английском.
       |Например _responsibility_ или _договор_ .
       |
       |Для получения толкования слова -- добавьте перед словом знак вопроса:
       |_?luxury_ или _?бурбон_ .
       |
       |/ask - связаться с создателями бота.
       |/help - вывести это сообщение.       |
     """.stripMargin
}


class HelpHandler extends CommandHandler[Help] {
  override def handle(command: Help): Future[Outcome] =
    Future.successful {
      SendMessage(command.chatId, HelpHandler.message)
    }
}
