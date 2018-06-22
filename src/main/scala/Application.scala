import configuration.Configuration
import http.Server

object Application {

  def main(args: Array[String]): Unit = {
    val configuration = Configuration.properties
    val port = configuration.ufag.port

    val server = new Server(port)
    val runningServer = server.start()

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        runningServer.stop()
      }
    })
  }

}
