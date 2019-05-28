package http

import akka.http.scaladsl.server.Route
import cats.effect.{Effect, Sync}
import model.telegram.Update
import model.telegram.Ok
import tapir._
import tapir.json.circe._
import tapir.server.akkahttp._
import telegram.TelegramUpdateHandler

import scala.concurrent.Future

class TelegramRouter[F[_]: Effect](
  telegramUpdateHandler: TelegramUpdateHandler[F]
) extends Router[F] {

  def route: Route =
    endpoint
      .post
      .in("ufag")
      .in(jsonBody[Update])
      .out(jsonBody[Ok])
      .toRoute(handle)

  // internal

  private def handle(request: Update): Future[Either[Unit, Ok]] =
    toFuture(
      telegramUpdateHandler.handle(request)
    )

}

object TelegramRouter {

  def create[F[_]: Effect](handler: TelegramUpdateHandler[F])(implicit F: Sync[F]): F[TelegramRouter[F]] =
    F.delay(new TelegramRouter(handler))

}
