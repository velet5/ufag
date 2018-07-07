package telegram

import bot.{CannotHandle, Ignore, NewBot, SendMessage}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configuration.Configuration
import http.Client
import lingvo.Lingvo
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicHeader
import org.slf4j.LoggerFactory
import persistence.{Db, Memory}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Bot {

  import scala.concurrent.ExecutionContext.Implicits.global

  val token: String = Configuration.properties.telegram.token
  val url = s"${Configuration.properties.ufag.serviceUrl}/ufag"
  val telegramApi = "https://api.telegram.org/bot"
  val setWebhook = "/setWebhook"
  val sendMessage_ = "/sendMessage"
  val forwardMessage_ = "/forwardMessage"
  val setWebhookUri: String = telegramApi + token + setWebhook + "?url=" + url
  val sendMessageUri: String = telegramApi + token + sendMessage_
  val forwardMessageUri: String = telegramApi + token + forwardMessage_

  private val log = LoggerFactory.getLogger(getClass)

  private val client = new Client
  private val db = new Db
  private val lingvo = new Lingvo(client, db)
  private val memory = new Memory(db)
  private val ask = new Ask(this, db)

  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule).setSerializationInclusion(Include.NON_NULL)

  private val newBot = new NewBot


  def process(json: String): Unit = {
    log.info(s"Processing $json")
    val update = mapper.readValue(json, classOf[Update])
    process(update)
  }


  def process(update: Update): Unit = {
    newBot.process(update).map {
      case CannotHandle => process0(update)
      case SendMessage(chatId, text) => sendMessage(TelegramSendMessage(chatId.value, text))
      case Ignore => log.info("Ignoring update")
    }
  }

  def process0(update: Update): Unit = {
    if (ask.isAsk(update)) {
      log.info("Asking detected")
      for {
        message <- update.message
        reply <- message.replyToMessage
        from <- reply.forwardFrom
        text <- message.text
      } {
        val eventualOption = db.getAsking(from.id, reply.messageId)
        eventualOption.foreach(_.foreach {asking =>
          sendMessage(TelegramSendMessage(from.id, text, replyToMessageId = Some(asking.originalMessageId)))
        })
      }
      return 
    }

    for {
      message <- update.message
      messageText <- message.text
      text = messageText.toLowerCase
    } {
      val chatId = message.chat.id
      val entities = message.entities

      if (entities.exists(_.exists(_.`type` == "bot_command"))) {
        log.info(s"Got command $text")

        if (text == "/ask" || text.startsWith("/ask ")) {
          ask.process(message)
        } else {
          sendMessage(TelegramSendMessage(chatId, text = s"Неизвестная команда $text"))
        }
      } else {
        lingvo
          .translate(text)
          .map { value =>
            memory
              .recall(chatId, text)
              .map {
                case None =>
                  process(TelegramSendMessage(chatId, value), chatId, text, remember = true)

                case Some(occurance) =>
                  process(
                    TelegramSendMessage(chatId, memory.fag(occurance), replyToMessageId = Some(occurance.messageId)),
                    chatId,
                    text,
                    remember = true,
                    messageId = Some(occurance.messageId))
              }
          }
          .recover {
            case ex =>
              log.error("Translation error", ex)
              process(TelegramSendMessage(chatId, ex.getMessage), chatId, text)
          }
      }
    }
  }
  

  private def process(value: TelegramSendMessage,
                      chatId: Long,
                      searchText: String,
                      remember: Boolean = false,
                      messageId: Option[Long] = None): Unit = {
    val eventualResponse = sendMessage(value)

    eventualResponse onComplete {
      case Success(response) =>
        response.body.foreach {body =>
          val tried = Try(mapper.readValue(body, classOf[TelegramResponse]))
          tried.foreach {tr =>
            if (remember) {
              memory.remember(chatId, searchText, messageId.getOrElse(tr.result.messageId))
            }
          }
          tried.failed.foreach(log.error(s"Can't process $value", _))
        }

      case Failure(ex) => log.error(s"Can't perform request $value", ex)
    }
  }


  def sendMessage(response: TelegramSendMessage): Future[Client.Response] = {
    val text = mapper.writeValueAsString(response)
    val contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString)
    val eventualResponse = client.post(sendMessageUri, text, contentType)

    eventualResponse.failed.foreach(log.error(s"Can't send message $response", _))

    eventualResponse
  }

  def forwardMessage(forwardingResponse: ForwardMessage): Future[Client.Response] = {
    val text = mapper.writeValueAsString(forwardingResponse)
    val contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString)
    val eventualResponse = client.post(forwardMessageUri, text, contentType)

    eventualResponse
  }
  
}
