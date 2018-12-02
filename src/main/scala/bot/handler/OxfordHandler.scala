package bot.handler

import bot.{Outcome, Oxford, SendMessage}

import scala.concurrent.{ExecutionContext, Future}

class OxfordHandler(ox: oxford.OxfordService)
                   (implicit ec: ExecutionContext) extends CommandHandler[Oxford] {

  override def handle(command: Oxford): Future[Outcome] =
    ox
      .define(command.word)
      .map(SendMessage(command.chatId, _))

}
