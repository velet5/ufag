import cats.effect.{Effect, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import wiring._

case class Application[F[_]](
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
      telegramModule <- TelegramModule.create(commonModule)
      botModule <- BotModule.create(telegramModule)
      serviceModule <- ServiceModule.create(botModule)
      httpModule <- HttpModule.create(serviceModule)
    } yield Application(
      commonModule,
      httpModule,
      telegramModule,
      botModule,
      serviceModule,
    )

}
