import configuration.Configuration
import http.Server
import org.slf4j.LoggerFactory

object Application {

  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    log.info("Application is starting up")

    val configuration = Configuration.properties
    val port = configuration.ufag.port

    val server = new Server(port)
    val runningServer = server.start()

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        log.info("application is stopping")
        runningServer.stop()
      }
    })
  }

}
