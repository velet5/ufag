package bot.handler

import bot.{Outcome, SendMessage, Statistics}
import persistence.Memory

import scala.concurrent.Future

class StatisticsHandler(memory: Memory) extends CommandHandler[Statistics] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def handle(command: Statistics): Future[Outcome] = {
      memory.stat().map { stat =>
        val statText =
          s"*Пользователей*: ${stat.userCount}.\n" +
            s"*Запросов*: ${stat.queryCount}.\n" +
            s"*Слов запомнено*: ${stat.wordCount}."

        SendMessage(command.chatId, statText)
    }
  }
  
}
