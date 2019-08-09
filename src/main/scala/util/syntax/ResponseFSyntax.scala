package util.syntax

import cats.MonadError
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.softwaremill.sttp.Response
import util.syntax.ResponseFSyntax.ResponseFOps

import scala.util.control.NoStackTrace

trait ResponseFSyntax {

  final implicit def syntaxResponseF[F[_] : MonadError[*[_], Throwable], A](
    responseF: F[Response[A]]
  ): ResponseFOps[F, A] =
    new ResponseFOps(responseF)

}

object ResponseFSyntax {

  final class ResponseFOps[F[_], A](
    responseF: F[Response[A]]
  )(
    implicit F: MonadError[F, Throwable],
  ) {

    def extract(): F[A] =
      responseF.flatMap(response =>
        response.body.fold(
          ResponseError(response.code, _).raiseError,
          _.pure[F]
        )
      )

  }

  case class ResponseError(code: Int, message: String)
    extends RuntimeException(s"Unexpected response: $code - $message")
      with NoStackTrace

}
