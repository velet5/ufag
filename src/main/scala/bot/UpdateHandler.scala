package bot

import bot.UpdateHandler.Fail
import cats.MonadError
import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.functor._
import model.telegram.{Ok, Update}

class UpdateHandler[F[_]](
  handlers: List[Handler[F, _]]
)(
  implicit F: MonadError[F, Throwable]
) {

  def process(update: Update): F[Ok] =
    handlers
      .map(_.handle(update))
      .collectFirst { case Some(fu) => fu }
      .getOrElse(Fail.raiseError)
      .map(_ => Ok())

}

object UpdateHandler {

  def create[F[_]](
    handlers: List[Handler[F, _]]
  )(
    implicit F: Sync[F]
  ): F[UpdateHandler[F]] =
    F.delay(new UpdateHandler[F](handlers))

  object Fail extends RuntimeException("Failed to find handler for the request")

}
