import cats.effect.{Effect, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import conf.Configuration
import wiring._

case class Application[F[_]](
  configuration: Configuration,
  commonModule: CommonModule[F],
  httpModule: HttpModule,
  telegramModule: TelegramModule[F],
  botModule: BotModule[F],
  serviceModule: ServiceModule[F],
)

object Application {

  def resource[F[_]](implicit F: Effect[F]): Resource[F, Application[F]] =
    CommonModule.resource[F].evalMap(makeApplication(_))

  // internal

  private def makeApplication[F[_] : Effect](
    commonModule: CommonModule[F]
  ): F[Application[F]] =
    for {
      config <- Configuration.create
      telegramModule <- TelegramModule.create(commonModule)
      botModule <- BotModule.create(telegramModule)
      serviceModule <- ServiceModule.create(botModule)
      httpModule <- HttpModule.create(serviceModule)
    } yield Application(
      config,
      commonModule,
      httpModule,
      telegramModule,
      botModule,
      serviceModule,
    )

}
