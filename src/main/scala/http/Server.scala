package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import cats.syntax.option.catsSyntaxOptionId
import org.slf4j.LoggerFactory
import telegram.UpdateHandler

import scala.concurrent.{ExecutionContext, Future}


class Server(port: Int, updateHandler: UpdateHandler) extends Directives {

  private var bindingFuture: Option[Future[Http.ServerBinding]] = None

  private implicit val system: ActorSystem = ActorSystem("my-system")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val route: Route =
    path("ufag") {
      post {
        entity(as[String]) {text =>
          updateHandler.handle(text)
          complete("")
        }
      }
    }

  def start(): Unit = {
    if (bindingFuture.isEmpty) {
      bindingFuture = Http().bindAndHandle(route, "localhost", port).some
    }
  }

  def stop(): Unit = {
    bindingFuture.foreach(
      _.flatMap(_.unbind())
        .onComplete(_ => {
          log.info("terminating")
          system.terminate()
        })
    )
  }

  // under the hood

  private val log = LoggerFactory.getLogger(getClass)

}
