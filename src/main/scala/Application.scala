import cats.effect.{ConcurrentEffect, Effect, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import slick.dbio.DBIO
import wiring._

case class Application[F[_], Db[_]](
  commonModule: CommonModule[F, Db],
  httpModule: HttpModule,
  telegramModule: TelegramModule[F],
  botModule: BotModule[F],
  serviceModule: ServiceModule[F],
)

object Application {

  def resource[F[_] : ConcurrentEffect]: Resource[F, Application[F, DBIO]] =
    CommonModule.resource[F].evalMap(makeApplication(_))

  // internal

  private def makeApplication[F[_] : Effect](
    commonModule: CommonModule[F, DBIO]
  ): F[Application[F, DBIO]] =
    for {
      telegramModule <- TelegramModule.create(commonModule)
      repositoryModule <- RepositoryModule.create
      serviceModule <- ServiceModule.create(repositoryModule)
      botModule <- BotModule.create(commonModule.transact, telegramModule, repositoryModule)
      httpModule <- HttpModule.create(botModule)
    } yield Application(
      commonModule,
      httpModule,
      telegramModule,
      botModule,
      serviceModule,
    )

}
