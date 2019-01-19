import org.slf4j.LoggerFactory

object Application extends Wiring {

  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    log.info("Application is starting up")

    server.start()

    monster.demonstrateSignsOfLiving()

    sys.addShutdownHook(() => {
      log.info("application is stopping")
      server.stop()
    })
  }

}
