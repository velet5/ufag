package old.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import cats.syntax.option.catsSyntaxOptionId
import org.slf4j.LoggerFactory
import old.telegram.RequestHandler

import scala.concurrent.{ExecutionContext, Future}


class Server(port: Int, updateHandler: RequestHandler)
            (implicit executionContext: ExecutionContext,
                      actorSystem: ActorSystem,
                      actorMaterializer: ActorMaterializer) extends Directives {

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
      bindingFuture = Http().bindAndHandle(route, "0.0.0.0", port).some
    }
  }

  def stop(): Unit = {
    bindingFuture.foreach(
      _.flatMap(_.unbind())
        .onComplete(_ => {
          log.info("terminating")
        })
    )
  }

  // under the hood

  private val log = LoggerFactory.getLogger(getClass)

  private var bindingFuture: Option[Future[Http.ServerBinding]] = None

}
