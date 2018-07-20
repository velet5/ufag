package bot.handler

import bot.{Outcome, RuDefine, SendMessage}

import scala.concurrent.Future

class RuDefineHandler(li: lingvo.Lingvo) extends CommandHandler[RuDefine] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle(command: RuDefine): Future[Outcome] =
    li
      .defineRussian(command.word)
      .map(_.fold(identity[String], identity[String]))
      .map(SendMessage(command.chatId, _))
  
}
