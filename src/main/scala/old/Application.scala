package old

import io.sentry.Sentry
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object Application extends App with BotWiring {

  withErrorLogging {
    onStart()
  }

  // private

  private def onStart()= {
    // INITIALIZE error-collecting
    Sentry.init(properties.sentry.dsn)

    log.info("Application is starting up")

    // LISTEN to incoming HTTP-requests
    server.start()

    // NOTIFY owner that app is started
    monster.demonstrateSignsOfLiving()

    // SETUP shutdown actions
    sys.addShutdownHook(() => {
      log.info("application is stopping")
      server.stop()
    })
  }

  def withErrorLogging(action: => Unit): Unit =
    try {
      action
    } catch {
      case NonFatal(ex) =>
        log.error("Error on application startup", ex)
        throw ex
    }

  private lazy val log = LoggerFactory.getLogger(getClass)

}
