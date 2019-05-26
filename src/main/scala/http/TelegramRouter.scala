package http

import akka.http.scaladsl.server.Route
import cats.effect.Sync
import conf.Configuration.TelegramProperties
import model.telegram.TelegramUpdate
import tapir._
import tapir.json.circe._
import tapir.server.akkahttp._

import scala.concurrent.Future

class TelegramRouter {

  def route: Route =
    endpoint
      .post
      .in("ufag")
      .in(jsonBody[TelegramUpdate])
      .toRoute(handle)

  // internal

  private def handle(request: TelegramUpdate): Future[Either[Unit, Unit]] = ???

}

object TelegramRouter {

  def create[F[_]](telegramProperties: TelegramProperties)(implicit F: Sync[F]): F[TelegramRouter] =
    F.delay(new TelegramRouter)

}
