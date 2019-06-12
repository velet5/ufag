package http

import akka.http.scaladsl.server.Route
import cats.effect.{Effect, Sync}
import model.telegram.Update
import model.telegram.Ok
import service.UpdateProcessingService
import tapir._
import tapir.json.circe._
import tapir.server.akkahttp._

import scala.concurrent.Future

class TelegramRouter[F[_]: Effect](
  telegramUpdateHandler: UpdateProcessingService[F]
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
      telegramUpdateHandler.process(request)
    )

}

object TelegramRouter {

  def create[F[_]: Effect](handler: UpdateProcessingService[F])(implicit F: Sync[F]): F[TelegramRouter[F]] =
    F.delay(new TelegramRouter(handler))

}
