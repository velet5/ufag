package util.syntax

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.flatMap._
import com.softwaremill.sttp.{MonadError => _, _}
import io.circe.Error

import scala.util.control.NoStackTrace

trait JsonResponseFSyntax {
  final implicit def syntaxJsonResponse[F[_], A](
    responseF: F[Response[Either[DeserializationError[Error], A]]]
  )(
    implicit F: MonadError[F, Throwable],
  ): JsonResponseFOps[F, A] =
    new JsonResponseFOps(responseF)
}

private[syntax]
final class JsonResponseFOps[F[_], A](
  private val responseF: F[Response[Either[DeserializationError[Error], A]]]
)(
  implicit F: MonadError[F, Throwable],
) {

  import JsonResponseError._

  def extract(): F[A] =
    responseF
      .flatMap(response =>
        response.body match {
          case Left(message) =>
            F.raiseError(NotOkError(response.code, message))

          case Right(either) =>
            either match {
              case Left(error) =>
                F.raiseError(ParsingError(error.message, error.error.getCause))

              case Right(value) =>
                value.pure
            }
        })

}

sealed abstract class JsonResponseError(message: String, cause: Throwable)
  extends RuntimeException(message, cause)
    with NoStackTrace

object JsonResponseError {

  case class NotOkError(statusCode: Int, message: String) extends JsonResponseError(message, null)

  case class ParsingError(message: String, cause: Throwable) extends JsonResponseError(message, cause)

}

