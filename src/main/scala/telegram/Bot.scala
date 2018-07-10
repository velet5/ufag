package telegram

import bot._
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import http.Client
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

class Bot {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = LoggerFactory.getLogger(getClass)

  private val client = new Client
  private val telegram = new Telegram(client)

  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule).setSerializationInclusion(Include.NON_NULL)

  private val newBot = new NewBot

  def process(json: String): Unit = {
    log.info(s"Processing $json")
    val update = mapper.readValue(json, classOf[Update])
    process(update)
  }

  def process(update: Update): Unit = {
    newBot
      .process(update)
      .map {
        case CannotHandle => process0(update)
        case SendMessage(chatId, text) => telegram.sendMessage(TelegramSendMessage(chatId.value, text))
        case ForwardMessage(senderChatId, receiverChatId, messageId) =>
          telegram.forwardMessage(TelegramForwardMessage(senderChatId.value, receiverChatId.value, messageId))
        case Ignore => log.info("Ignoring update")
      }
      .recover { case NonFatal(ex) => log.error("While processing update", ex) }
  }

  def process0(update: Update): Unit = {
    for {
      message <- update.message
      messageText <- message.text
      text = messageText.toLowerCase
    } {
      val chatId = message.chat.id
      val entities = message.entities

      if (entities.exists(_.exists(_.`type` == "bot_command"))) {
        log.info(s"Got command $text")
        telegram.sendMessage(TelegramSendMessage(chatId, text = s"Неизвестная команда $text"))
      } else {
        
      }
    }
  }
  
}
