package old.bot.action

import old.bot.{Outcome, RuDefine, SendMessage}

import scala.concurrent.{ExecutionContext, Future}

class RuDefineAction(li: old.lingvo.LingvoService)
                     (implicit ec: ExecutionContext) extends CommandAction[RuDefine] {

  override def run(command: RuDefine): Future[Outcome] =
    li
      .defineRu(command.word)
      .map(_.fold(identity[String], identity[String]))
      .map(SendMessage(command.chatId, _))
  
}
