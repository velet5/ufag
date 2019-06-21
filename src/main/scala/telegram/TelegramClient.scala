package telegram

import cats.MonadError
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.option._
import com.softwaremill.sttp.circe._
import com.softwaremill.sttp.{Response => _, _}
import conf.Configuration.TelegramProperties
import conf.TelegramConfig
import io.circe.Error
import model.telegram.Sending.ParseMode
import model.telegram.{Chat, Message, Response, Sending}

import scala.util.control.NoStackTrace

trait TelegramClient[F[_]] {
  def send(chatId: Chat.Id, message: String): F[Message]
  def forward(from: Chat.Id, to: Chat.Id, messageId: Message.Id): F[Message]
}

object TelegramClient {

  def create[F[_] : Sync](
    telegramProperties: TelegramProperties,
  )(
    implicit sttpBackend: SttpBackend[F, Nothing]
  ): F[TelegramClient[F]] =
    Sync[F].delay(
      new Impl[F](
        TelegramConfig.fromConfiguration(telegramProperties)
      )
    )

  class Impl[F[_] : MonadError[?[_], Throwable]](
    telegramConfig: TelegramConfig,
  )(
    implicit sttpBackend: SttpBackend[F, Nothing]
  ) extends TelegramClient[F] {

    override def send(chatId: Chat.Id, markdown: String): F[Message] =
      sttp
        .post(telegramConfig.sendMessageUri)
        .body(Sending(chatId, markdown, ParseMode.Markdown))
        .response(asJson[Response[Message]])
        .send()
        .flatMap(response => processNetworkResponse(response.body))

    override def forward(
      from: Chat.Id,
      to: Chat.Id,
      messageId: Message.Id
    ): F[Message] =
      ???


    // internal


    private def processNetworkResponse(either: Either[
      String,
      Either[
        DeserializationError[Error],
        Response[Message]]]
    ): F[Message] =
      either.fold(
        message => MonadError[F, Throwable].raiseError(SendingError.NetworkError(message)),
        processParsing
      )

    private def processParsing(either: Either[DeserializationError[Error], Response[Message]]): F[Message] =
      either.fold(
        error => MonadError[F, Throwable].raiseError(SendingError.ParsingError(error)),
        processTelegramResponse
      )

    private def processTelegramResponse(response: Response[Message]): F[Message] =
      response.value
        .liftTo(SendingError.TelegramError(
          response.description.getOrElse("Неизвестная ошибка")
        ))

  }

  sealed abstract class SendingError(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)
      with NoStackTrace

  object SendingError {

    case class NetworkError(message: String) extends SendingError(message)

    case class ParsingError(error: DeserializationError[Error]) extends SendingError(error.message)

    case class TelegramError(message: String) extends SendingError(message)

  }

}
