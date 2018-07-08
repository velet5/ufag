package telegram

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configuration.Configuration
import http.Client
import http.Client.Response
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicHeader

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Telegram {
  private val token: String = Configuration.properties.telegram.token
  private val url = s"${Configuration.properties.ufag.serviceUrl}/ufag"
  private val telegramApi = "https://api.telegram.org/bot"
  private val setWebhookUri: String = telegramApi + token + "/setWebhook" + "?url=" + url
  private val sendMessageUri: String = telegramApi + token + "/sendMessage"
  private val forwardMessageUri: String = telegramApi + token + "/forwardMessage"

  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule).setSerializationInclusion(Include.NON_NULL)
}

class Telegram(client: Client) {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Telegram._

  def sendMessage(message: TelegramSendMessage): Future[TelegramResponse] =
    executeMessage(message, sendMessageUri)

  def forwardMessage(message: TelegramForwardMessage): Future[TelegramResponse] =
    executeMessage(message, forwardMessageUri)

  private def executeMessage[M](message: M, uri: String): Future[TelegramResponse] =
    performHttp(message, uri).flatMap(r => Future.fromTry(toTelegramResponse(r)))

  private def toTelegramResponse(response: Response): Try[TelegramResponse] =
    response.body match {
      case Some(body) => Success(mapper.readValue[TelegramResponse](body, classOf[TelegramResponse]))
      case None => Failure(new RuntimeException("Telegram responded invalid body"))
    }

  private def performHttp[M](message: M, uri: String): Future[Response] = {
    val text = mapper.writeValueAsString(message)
    val contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString)
    
    client.post(uri, text, contentType)
  }

}
