package bot.handler

import bot.{Ignore, Lingvo, Outcome}
import org.slf4j.LoggerFactory
import persistence.{Db, Memory, Occurance}
import telegram.{Telegram, TelegramSendMessage}

import scala.concurrent.Future

private object LingvoHandler {
  case class Message(message: TelegramSendMessage, remember: Boolean)
}

class LingvoHandler(db: Db, memory: Memory, telegram: Telegram, li: lingvo.Lingvo) extends CommandHandler[Lingvo] {

  import LingvoHandler._

  import scala.concurrent.ExecutionContext.Implicits.global
  private val log = LoggerFactory.getLogger(getClass)

  override def handle(command: Lingvo): Future[Outcome] = {
    val text = command.word
    val chatId = command.chatId.value

    val occuranceToMessage: Option[Occurance] => Future[Message] = {
      case Some(occurance) =>
        val message = TelegramSendMessage(chatId, memory.fag(occurance), replyToMessageId = Some(occurance.messageId))
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
        message <- occuranceToMessage(maybeOccurance)
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
