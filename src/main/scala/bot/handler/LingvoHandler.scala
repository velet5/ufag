package bot.handler

import bot.{Ignore, Lingvo, Outcome}
import org.slf4j.LoggerFactory
import persistence.model.Occurrence
import persistence.{Db, Memory}
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.{ExecutionContext, Future}

private object LingvoHandler {
  case class Message(message: TelegramSendMessage, remember: Boolean)
}

class LingvoHandler(db: Db, memory: Memory, telegram: Telegram, li: lingvo.Lingvo)
                   (implicit ec: ExecutionContext) extends CommandHandler[Lingvo] {

  import LingvoHandler._

  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Lingvo): Future[Outcome] = {
    val text = command.word
    val chatId = command.chatId.value

    val occurrenceToMessage: Option[Occurrence] => Future[Message] = {
      case Some(occurrence) =>
        val message = TelegramSendMessage(chatId, memory.fag(occurrence), replyToMessageId = Some(occurrence.messageId))
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
        maybeOccurance <- memory.recall(chatId, text)
        message <- occurrenceToMessage(maybeOccurance)
        response <- telegram.sendMessage(message.message)
        messageId = maybeOccurance.fold(response.result.messageId)(_.messageId)
        _ = if (message.remember) memory.remember(chatId, text, messageId)
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
