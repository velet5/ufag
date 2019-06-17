package wiring

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.arrow.FunctionK
import cats.effect.{Async, ConcurrentEffect, Resource, Sync}
import cats.syntax.applicative._
import cats.~>
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.softwaremill.sttp.{SttpBackend, SttpBackendOptions}
import conf.Configuration
import conf.Configuration.PostgresProperties
import monix.catnap.syntax.SyntaxForLiftFuture
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.DurationLong

case class CommonModule[F[_], Db[_]](
  actorSystem: ActorSystem,
  actorMaterializer: ActorMaterializer,
  sttpBackend: SttpBackend[F, Nothing],
  configuration: Configuration,
  transact: Db ~> F,
)

object CommonModule {

  def resource[F[_] : ConcurrentEffect]: Resource[F, CommonModule[F, DBIO]] =
    for {
      actorSystem <- makeActorSystem
      actorMaterializer <- makeMaterializer(actorSystem)
      sttpBackend <- makeSttpBackend
      configuration <- Resource.liftF(Configuration.create)
      transact <- Resource.liftF(makeTransact[F](configuration.postgres))
    } yield CommonModule(
      actorSystem,
      actorMaterializer,
      sttpBackend,
      configuration,
      transact,
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
      Sync[F].delay(AsyncHttpClientCatsBackend[F](
        SttpBackendOptions.connectionTimeout(15.seconds)
      ))
    )(
      backend => Sync[F].delay(backend.close())
    )

  private def makeTransact[F[_] : ConcurrentEffect](config: PostgresProperties): F[DBIO ~> F] = {
    val db = Database.forDriver(
      driver = new org.postgresql.Driver(),
      url = config.connectionString,
      user = config.user,
      password = config.password
    )

    def f[A](v: DBIO[A]): F[A] =
      Sync[F].delay(db.run(v)).futureLift

    FunctionK.lift[DBIO, F](f).pure
  }

}
