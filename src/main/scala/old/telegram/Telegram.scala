package old.telegram

import _root_.old.client._
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait Telegram[F[_]] {
  def sendMessage(message: TelegramSendMessage): F[TelegramResponse]
  def forwardMessage(message: TelegramForwardMessage): F[TelegramResponse]
}

class TelegramImpl(
  token: String,
  client: RestClient,
  mapper: ObjectMapper
)(
  implicit ec: ExecutionContext
) extends Telegram[Future]  {

  def sendMessage(message: TelegramSendMessage): Future[TelegramResponse] =
    executeMessage(message, sendMessageUri)

  def forwardMessage(message: TelegramForwardMessage): Future[TelegramResponse] =
    executeMessage(message, forwardMessageUri)

  // private

  private def executeMessage[M](message: M, uri: String): Future[TelegramResponse] =
    performHttp(message, uri).flatMap(r => Future.fromTry(toTelegramResponse(r)))

  private def toTelegramResponse(response: Response): Try[TelegramResponse] =
    response.bodyOpt match {
      case Some(body) => Success(mapper.readValue[TelegramResponse](body.value, classOf[TelegramResponse]))
      case None => Failure(new RuntimeException("Telegram responded invalid body"))
    }

  private def performHttp[M](message: M, uri: String): Future[Response] = {
    val text = mapper.writeValueAsString(message)
    val contentType = Header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString)
    
    client.post(Uri(uri), contentType, Body(text))
  }

  private val telegramApi = "https://api.old.telegram.org/old.bot"
  private val sendMessageUri: String = telegramApi + token + "/sendMessage"
  private val forwardMessageUri: String = telegramApi + token + "/forwardMessage"

}
