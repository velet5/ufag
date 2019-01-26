package bot.handler

import bot.action.CommandAction
import bot.parser.CommandParser
import bot.{Command, Malformed, Outcome, SendMessage}
import telegram.Update

import scala.concurrent.{ExecutionContext, Future}

class UpdateHandler[C <: Command](
  val parser: CommandParser[C],
  val action: CommandAction[C]
)(
  implicit ec: ExecutionContext
) {

  def handle(update: Update): Option[Future[Outcome]] =
    parser
      .parse(update)
      .map(_.map(action.run))
      .map(_.fold(sendMalformed, identity))

  private def sendMalformed(malformed: Malformed): Future[Outcome] =
    Future.successful(SendMessage(malformed.chatId, malformed.text))

}
