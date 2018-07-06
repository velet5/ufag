package bot.handler

import bot.{Help, Outcome, SendMessage}

import scala.concurrent.Future

private object HelpHandler {
  private val message =
    "Бот работает как англо-русский словарь.\n" +
      "Просто напишите ему слово и он сделает всё возможное чтобы найти словарную статью.\n" +
      "Не является полноценным переводчиком, вы не можете переводить с его помощью целые предложения.\n" +
      "Если вы в течение месяца спросите одно и то же слово более одного раза - бот назовёт вас п\\*дором.\n" +
      "И помните - учите английский, а то чо как эти в самом деле.\n\n" +
      "Использует API https://www.lingvolive.com/"
}


class HelpHandler extends CommandHandler[Help] {
  override def handle(command: Help): Future[Outcome] =
    Future.successful {
      SendMessage(command.chatId, HelpHandler.message)
    }
}
