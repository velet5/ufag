package wiring

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.{Async, Resource, Sync}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend

case class CommonModule[F[_]](
  actorSystem: ActorSystem,
  actorMaterializer: ActorMaterializer,
  sttpBackend: SttpBackend[F, Nothing],
)

object CommonModule {

  def resource[F[_] : Async]: Resource[F, CommonModule[F]] =
    for {
      actorSystem <- makeActorSystem
      actorMaterializer <- makeMaterializer(actorSystem)
      sttpBackend <- makeSttpBackend
    } yield CommonModule(
      actorSystem,
      actorMaterializer,
      sttpBackend,
    )

  private def makeActorSystem[F[_] : Sync]: Resource[F, ActorSystem] =
    Resource.make(
      Sync[F].delay(ActorSystem())
    )(
      system => Sync[F].delay(system.terminate())
    )

  private def makeMaterializer[F[_] : Sync](actorSystem: ActorSystem): Resource[F, ActorMaterializer] =
    Resource.make(
      Sync[F].delay(ActorMaterializer()(actorSystem))
    )(
      materializer => Sync[F].delay(materializer.shutdown())
    )

  private def makeSttpBackend[F[_] : Async]: Resource[F, SttpBackend[F, Nothing]] =
    Resource.make(
      Sync[F].delay(AsyncHttpClientCatsBackend[F]())
    )(
      backend => Sync[F].delay(backend.close())
    )

}
