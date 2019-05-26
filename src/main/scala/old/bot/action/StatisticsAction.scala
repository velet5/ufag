package old.bot.action

import old.bot.{Outcome, SendMessage, Statistics}
import old.service.QueryService

import scala.concurrent.{ExecutionContext, Future}

class StatisticsAction(queryService: QueryService)(implicit ec: ExecutionContext)
  extends CommandAction[Statistics] {

  override def run(command: Statistics): Future[Outcome] =
    queryService
      .count()
      .map(count => SendMessage(command.chatId, s"*Запросов*: $count."))
  
}
