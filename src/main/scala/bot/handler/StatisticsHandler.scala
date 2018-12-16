package bot.handler

import bot.{Outcome, SendMessage, Statistics}
import service.QueryService

import scala.concurrent.Future

class StatisticsHandler(queryService: QueryService) extends CommandHandler[Statistics] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle(command: Statistics): Future[Outcome] = {
    queryService.count()
      .map(count => SendMessage(command.chatId, s"*Запросов*: $count."))
  }
  
}
