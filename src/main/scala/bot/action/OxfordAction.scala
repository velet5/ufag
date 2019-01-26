package bot.action

import bot.{Outcome, Oxford, SendMessage}

import scala.concurrent.{ExecutionContext, Future}

class OxfordAction(ox: oxford.OxfordService)
                   (implicit ec: ExecutionContext) extends CommandAction[Oxford] {

  override def run(command: Oxford): Future[Outcome] =
    ox
      .define(command.word)
      .map(SendMessage(command.chatId, _))

}
