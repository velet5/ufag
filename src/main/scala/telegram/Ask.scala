package telegram

import configuration.Configuration
import org.slf4j.LoggerFactory
import persistence.Db

import scala.concurrent.Future

class Ask(telegram: Bot, db: Db) {

  private val log = LoggerFactory.getLogger(getClass)

  private val ownerId = Configuration.properties.ufag.ownerId

  import scala.concurrent.ExecutionContext.Implicits.global

  def isAsk(update: Update): Boolean =
    update.message
      .exists(message =>
        message.chat.id == ownerId &&
        message.replyToMessage.exists(_.forwardFrom.nonEmpty))
  

  def process(message: Message): Future[Unit] = Future {
    message.text.foreach { text =>
      if (text.length > 5) {
        val forwardingResponse = ForwardMessage(
          ownerId,
          message.chat.id,
          message.messageId)

        telegram.forwardMessage(forwardingResponse) 
      } else {
        val response = TelegramSendMessage(message.chat.id, "Напишите ваше сообщение после `/ask `.")
        telegram.sendMessage(response) onComplete (_.failed.foreach(log.error("Can't send", _)))
      }
    }
  }

}
