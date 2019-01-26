package telegram

import bot._
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.control.NonFatal

trait RequestHandler {
  def handle(update: String): Unit
}

class RequestHandlerImpl(telegram: Telegram, bot: Bot)(implicit ec: ExecutionContext) extends RequestHandler {

  def handle(json: String): Unit = {
    log.info(s"Processing $json")
    val update = Try(mapper.readValue(json, classOf[Update]))
    
    update
      .map(process)
      .failed
      .foreach(log.error("Cannot parse update", _))
  }

  // under the hood

  private def process(update: Update): Unit = {
    bot
      .process(update)
      .map {
        case SendMessage(chatId, text) => telegram.sendMessage(TelegramSendMessage(chatId.value, text))
        case ForwardMessage(senderChatId, receiverChatId, messageId) =>
          telegram.forwardMessage(TelegramForwardMessage(senderChatId.value, receiverChatId.value, messageId))
        case Ignore => log.info("Ignoring update")
        case CannotHandle => log.info("Cannot handle update", update)
      }
      .recover { case NonFatal(ex) => log.error("While processing update", ex) }
  }

  private val log = LoggerFactory.getLogger(getClass)

  private val mapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .setSerializationInclusion(Include.NON_NULL)

}
