package wiring

import akka.http.scaladsl.server.Route
import cats.effect.Sync
import cats.syntax.functor._
import conf.Configuration
import http.TelegramRouter

case class HttpModule(
  route: Route,
)

object HttpModule {

  def create[F[_]](configuration: Configuration)(implicit F: Sync[F]): F[HttpModule] =
    TelegramRouter
      .create(configuration.telegram)
      .map(router => HttpModule(router.route))

}