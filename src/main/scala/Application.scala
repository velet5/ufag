import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.{Effect, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import conf.Configuration
import wiring.{HandlerModule, HttpModule, TelegramModule}

case class Application[F[_]](
  actorSystem: ActorSystem,
  actorMaterializer: ActorMaterializer,
  configuration: Configuration,
  httpModule: HttpModule,
  telegramModule: TelegramModule[F],
  handlerModule: HandlerModule[F],
)

object Application {

  def resource[F[_]](implicit F: Effect[F]): Resource[F, Application[F]] =
    makeResources().evalMap { case (s, m) => makeApplication(s, m) }

  // internal

  private def makeResources[F[_] : Sync](): Resource[F, (ActorSystem, ActorMaterializer)] =
    for {
      actorSystem <- makeActorSystem
      actorMaterializer <- makeMaterializer(actorSystem)
    } yield (actorSystem, actorMaterializer)

  private def makeApplication[F[_] : Effect](
    actorSystem: ActorSystem,
    actorMaterializer: ActorMaterializer
  ): F[Application[F]] =
    for {
      config <- Configuration.create
      telegramModule <- TelegramModule.create
      httpModule <- HttpModule.create(telegramModule)
      handlerModule <- HandlerModule.create
    } yield Application(
      actorSystem,
      actorMaterializer,
      config,
      httpModule,
      telegramModule,
      handlerModule,
    )

  private def makeActorSystem[F[_]](implicit F: Sync[F]): Resource[F, ActorSystem] =
    Resource.make(F.delay(ActorSystem()))(system => F.delay(system.terminate()))

  private def makeMaterializer[F[_]](actorSystem: ActorSystem)(implicit F: Sync[F]): Resource[F, ActorMaterializer] =
    Resource.make(
      F.delay(ActorMaterializer()(actorSystem))
    )(
      materializer => F.delay(materializer.shutdown())
    )

}
