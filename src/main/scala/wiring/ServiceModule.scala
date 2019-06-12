package wiring

import cats.effect.Sync
import cats.syntax.functor._
import service.UpdateProcessingService

case class ServiceModule[F[_]](
  updateProcessingService: UpdateProcessingService[F]
)

object ServiceModule {

  def create[F[_] : Sync](botModule: BotModule[F]): F[ServiceModule[F]] =
    for {
      updateProcessingService <- UpdateProcessingService.create(botModule.handlers)
    } yield ServiceModule(updateProcessingService)

}
