import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.{Resource, Sync}
import conf.Configuration
import wiring.HttpModule

case class Application(
  actorSystem: ActorSystem,
  actorMaterializer: ActorMaterializer,
  configuration: Configuration,
  httpModule: HttpModule,
)

object Application {

  def resource[F[_]](implicit F: Sync[F]): Resource[F, Application] =
    for {
      actorSystem <- makeActorSystem
      actorMaterializer <- makeMaterializer(actorSystem)
      config <- Resource.liftF(Configuration.create)
      httpModule <- Resource.liftF(HttpModule.create(config))
    } yield Application(
      actorSystem,
      actorMaterializer,
      config,
      httpModule
    )

  // internal

  private def makeActorSystem[F[_]](implicit F: Sync[F]): Resource[F, ActorSystem] =
    Resource.make(F.delay(ActorSystem()))(system => F.delay(system.terminate()))

  private def makeMaterializer[F[_]](actorSystem: ActorSystem)(implicit F: Sync[F]): Resource[F, ActorMaterializer] =
    Resource.make(
      F.delay(ActorMaterializer()(actorSystem))
    )(
      materializer => F.delay(materializer.shutdown())
    )

}
