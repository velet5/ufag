package service

import bot.Handler
import cats.MonadError
import cats.effect.Sync
import cats.syntax.functor._
import model.telegram.{Ok, Update}

class UpdateProcessingService[F[_]](
  handlers: List[Handler[F, _]]
)(
  implicit F: MonadError[F, Throwable]
) {

  def process(update: Update): F[Ok] =
    handlers
      .map(_.handle(update))
      .collectFirst { case Some(fu) => fu }
      .getOrElse(F.raiseError(new RuntimeException))
      .map(_ => Ok())

}

object UpdateProcessingService {

  def create[F[_]](
    handlers: List[Handler[F, _]]
  )(
    implicit F: Sync[F]
  ): F[UpdateProcessingService[F]] =
    F.delay(new UpdateProcessingService[F](handlers))

  case class Fail() extends RuntimeException()

}
