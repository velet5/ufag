package bot.handler

import bot.{Outcome, Oxford, SendMessage}

import scala.concurrent.Future

class OxfordHandler(ox: oxford.Oxford) extends CommandHandler[Oxford] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle(command: Oxford): Future[Outcome] =
    ox
      .define(command.word)
      .map(SendMessage(command.chatId, _))

}
