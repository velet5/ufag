package http

import cats.effect.Effect
import cats.effect.Effect.nonInheritedOps.toEffectOps
import cats.syntax.either._
import cats.syntax.functor._

import scala.concurrent.Future

trait Router[F[_]] {

  protected def toFuture[A](fa: F[A])(implicit F: Effect[F]): Future[Either[Nothing, A]] =
    fa
      .map(_.asRight[Nothing])
      .toIO
      .unsafeToFuture()

}
