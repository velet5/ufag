package telegram

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configuration.Configuration
import org.slf4j.LoggerFactory
import persistence.Db

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Ask(telegram: Telegram, db: Db) {

  private val log = LoggerFactory.getLogger(getClass)

  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule).setSerializationInclusion(Include.NON_NULL)
  private val ownerId = Configuration.properties.ufag.ownerId

  import scala.concurrent.ExecutionContext.Implicits.global

  def isAsk(update: Update): Boolean = {
    update.message.exists(message => message.chat.id == ownerId && message.replyToMessage.exists(_.forwardFrom.nonEmpty))
  }

  def process(message: Message): Future[Unit] = Future {
    message.text.foreach { text =>
      if (text.length > 5) {
        val forwardingResponse = ForwardMessage(
          ownerId,
          message.chat.id,
          message.messageId)

        telegram.forwardMessage(forwardingResponse) onComplete {
          case Success(response) =>
            response.body.foreach {body =>
              val tried = Try(mapper.readValue(body, classOf[TelegramResponse]))
              tried.foreach {tr =>
                if (tr.ok) {
                  db.saveAsking(message.chat.id, message.messageId, tr.result.messageId)
                }
              }
            }
            
          case Failure(ex) =>
            log.error(s"Can't forward $forwardingResponse", ex)
        }
      } else {
        val response = SendMessage(message.chat.id, "Напишите ваше сообщение после `/ask `.")
        telegram.sendMessage(response) onComplete (_.failed.foreach(log.error("Can't send", _)))
      }
    }
  }

}
