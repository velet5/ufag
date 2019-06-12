package old.bot.action

import old.bot.{Help, Outcome, SendMessage}

import scala.concurrent.Future

private object HelpAction {
  val message: String =
    s"""
       |Напишите слово для перевода, на русском или английском.
       |Например _responsibility_ или _договор_ .
       |
       |Для получения толкования слова -- добавьте перед словом знак вопроса:
       |_?luxury_ или _?бурбон_ .
       |
       |/ask - связаться с создателями бота.
       |/help - вывести это сообщение.
     """.stripMargin
}


class HelpAction extends CommandAction[Help] {
  override def run(command: Help): Future[Outcome] =
    Future.successful {
      SendMessage(command.chatId, HelpAction.message)
    }
}
