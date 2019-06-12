import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import monix.catnap.syntax._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO] >>= go

  // internal

  private def go(log: Logger[IO]) =
    makeBinding(log).use(runApplication(_, log).as(ExitCode.Success))

  private def makeBinding(log: Logger[IO]): Resource[IO, ServerBinding] =
    Application.resource[IO] >>= bindAkkaHttp(log)

  private def runApplication(binding: ServerBinding, log: Logger[IO]): IO[Unit] =
    log.info("Application started") >> IO(binding.whenTerminated).futureLift.void

  private def bindAkkaHttp(
    log: Logger[IO]
  )(
    application: Application[IO]
  ): Resource[IO, ServerBinding] = {
    implicit val actorSystem: ActorSystem = application.commonModule.actorSystem
    implicit val actorMaterializer: ActorMaterializer = application.commonModule.actorMaterializer

    val config = application.configuration.ufag
    val port = config.port
    val interface = "localhost"

    def acquire =
      for {
        _ <- log.info("Binding http")
        binding <- IO(Http().bindAndHandle(application.httpModule.route, interface, port)).futureLift
        _ <- log.info(s"Bound at $interface:$port")
      } yield binding

    def release(binding: ServerBinding) =
      log.info(s"Unbinding$interface:$port") >> IO(binding.unbind()).futureLift.void

    Resource.make(acquire)(release)
  }

}
