package wiring

import akka.http.scaladsl.server.Route
import cats.effect.Effect
import cats.syntax.functor._
import http.TelegramRouter

case class HttpModule(
  route: Route,
)

object HttpModule {

  def create[F[_]](serviceModule: ServiceModule[F])(implicit F: Effect[F]): F[HttpModule] =
    TelegramRouter
      .create(serviceModule.updateProcessingService)
      .map(router => HttpModule(router.route))

}