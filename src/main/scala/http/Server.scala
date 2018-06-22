package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import telegram.Telegram

import scala.concurrent.{ExecutionContext, Future}


class Server(port: Int) extends Directives {

  private implicit val system: ActorSystem = ActorSystem("my-system")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val telegram = new Telegram

  private val route: Route =
    path("ufag") {
      post {
        entity(as[String]) {text =>
          println("GOT REQUEST: " + text)
          telegram.process(text)
          complete("")
        }
      }
    }

  def start(): RunningServer = {
    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", port)

    new RunningServer(system, bindingFuture)
  }

}


class RunningServer(system: ActorSystem, bindingFuture: Future[Http.ServerBinding]) {
  private implicit val executionContext: ExecutionContext = system.dispatcher

  def stop(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => {
        system.terminate()
      })
  }

}
