import http.Server
import org.slf4j.LoggerFactory

object Application extends Wiring {

  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    log.info("Application is starting up")

    val server = new Server(port, updateHandler)
    server.start()

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        log.info("application is stopping")
        server.stop()
      }
    })
  }

}
