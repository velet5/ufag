package bot.handler

import bot.{Outcome, RuDefine, SendMessage}

import scala.concurrent.{ExecutionContext, Future}

class RuDefineHandler(li: lingvo.Lingvo)
                     (implicit ec: ExecutionContext) extends CommandHandler[RuDefine] {

  override def handle(command: RuDefine): Future[Outcome] =
    li
      .defineRussian(command.word)
      .map(_.fold(identity[String], identity[String]))
      .map(SendMessage(command.chatId, _))
  
}
