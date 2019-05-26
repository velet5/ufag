import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.Future

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO] >>= go

  // internal

  private def go(log: Logger[IO]) =
    makeBinding(log).use(runApplication(_, log).as(ExitCode.Success))

  private def makeBinding(log: Logger[IO]): Resource[IO, ServerBinding] =
    Application.resource[IO] >>= bindAkkaHttp(log)

  private def runApplication(binding: ServerBinding, log: Logger[IO]): IO[Unit] =
    log.info("Application started") >> fromFuture(binding.whenTerminated).void

  private def bindAkkaHttp(
    log: Logger[IO]
  )(
    application: Application[IO]
  ): Resource[IO, ServerBinding] = {
    implicit val actorSystem: ActorSystem = application.actorSystem
    implicit val actorMaterializer: ActorMaterializer = application.actorMaterializer

    val config = application.configuration.ufag
    val port = config.port
    val interface = "0.0.0.0"

    def acquire =
      for {
        _ <- log.info("Binding http")
        binding <- fromFuture(Http().bindAndHandle(application.httpModule.route, interface, port))
        _ <- log.info(s"Bound at $interface:$port")
      } yield binding

    def release(binding: ServerBinding) =
      fromFuture(binding.unbind()).void

    Resource.make(acquire)(release)
  }

  private def fromFuture[A](future: Future[A]): IO[A] =
    IO.fromFuture(IO.delay(future))

}
