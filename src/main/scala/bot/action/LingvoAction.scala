package bot.action

import bot.{Ignore, Lingvo, Outcome}
import org.slf4j.LoggerFactory
import persistence.Memory
import persistence.model.Query
import service.QueryService
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.{ExecutionContext, Future}

class LingvoAction(
  queryService: QueryService,
  telegram: Telegram,
  lingvoService: lingvo.LingvoService
)(
  implicit ec: ExecutionContext
) extends CommandAction[Lingvo] {

  import LingvoAction._

  private val log = LoggerFactory.getLogger(getClass)

  override def run(command: Lingvo): Future[Outcome] = {
    val text = command.word
    val chatId = command.chatId.value

    val queryToMessage: Option[Query] => Future[Message] = {
      case Some(query) =>
        val message = TelegramSendMessage(chatId, Memory.fag(query.time), replyToMessageId = Some(query.messageId))
        Future.successful(Message(message, remember = true))

      case None =>
        lingvoService
          .translate(text)
          .map(_.fold(
            l => Message(TelegramSendMessage(chatId, l), remember = false),
            r => Message(TelegramSendMessage(chatId, r), remember = true)))
    }

    val future =
      for {
        maybeQuery <- queryService.find(chatId, text)
        message <- queryToMessage(maybeQuery)
        response <- telegram.sendMessage(message.message)
        messageId = maybeQuery.fold(response.result.messageId)(_.messageId)
        _ = if (message.remember) queryService.save(chatId, text, messageId)
      } yield ()

    future
      .recoverWith {case ex: Throwable =>
        log.error(s"Cannot process lingvo [$text]", ex)
        telegram
          .sendMessage(TelegramSendMessage(chatId, "Ошибка обработки запроса"))
          .map(_ => Unit)
      }
      .map(_ => Ignore)
  }
  
}

private object LingvoAction {
  case class Message(message: TelegramSendMessage, remember: Boolean)
}
