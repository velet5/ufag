package bot.action

import bot.{Outcome, SendMessage, Statistics}
import service.QueryService

import scala.concurrent.{ExecutionContext, Future}

class StatisticsAction(queryService: QueryService)(implicit ec: ExecutionContext)
  extends CommandAction[Statistics] {

  override def run(command: Statistics): Future[Outcome] =
    queryService
      .count()
      .map(count => SendMessage(command.chatId, s"*Запросов*: $count."))
  
}
