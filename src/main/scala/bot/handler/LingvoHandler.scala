package bot.handler

import bot.{Ignore, Lingvo, Outcome}
import org.slf4j.LoggerFactory
import persistence.model.Query
import persistence.{Db, Memory}
import service.QueryService
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.{ExecutionContext, Future}

private object LingvoHandler {
  case class Message(message: TelegramSendMessage, remember: Boolean)
}

class LingvoHandler(db: Db, queryService: QueryService, telegram: Telegram, li: lingvo.Lingvo)
                   (implicit ec: ExecutionContext) extends CommandHandler[Lingvo] {

  import LingvoHandler._

  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Lingvo): Future[Outcome] = {
    val text = command.word
    val chatId = command.chatId.value

    val queryToMessage: Option[Query] => Future[Message] = {
      case Some(query) =>
        val message = TelegramSendMessage(chatId, Memory.fag(query.time), replyToMessageId = Some(query.messageId))
        Future.successful(Message(message, remember = true))

      case None =>
        li
          .translate(text)
          .map(_.fold(
            l => Message(TelegramSendMessage(chatId, l), remember = false),
            r => Message(TelegramSendMessage(chatId, r), remember = true)))
    }

    val eventualUnit =
      for {
        maybeQuery <- queryService.find(chatId, text)
        message <- queryToMessage(maybeQuery)
        response <- telegram.sendMessage(message.message)
        messageId = maybeQuery.fold(response.result.messageId)(_.messageId)
        _ = if (message.remember) queryService.save(chatId, text, messageId)
      } yield ()

    eventualUnit
      .recoverWith {case ex: Throwable =>
        log.error(s"Cannot process lingvo [$text]", ex)
        telegram
          .sendMessage(TelegramSendMessage(chatId, "Ошибка обработки запроса"))
          .map(_ => Unit)
      }
      .map(_ => Ignore)
  }
  
}
