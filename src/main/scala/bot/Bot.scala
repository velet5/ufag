package bot

import bot.handler.UpdateHandler
import telegram.{Telegram, Update}

import scala.concurrent.Future

trait Bot {
  def process(update: Update): Future[Outcome]
}

class BotImpl(telegram: Telegram, handlers: Seq[UpdateHandler[_]]) extends Bot {

  def process(update: Update): Future[Outcome] =
    handlers.view
      .flatMap(_.handle(update))
      .headOption
      .getOrElse(Future.successful(CannotHandle))

}
