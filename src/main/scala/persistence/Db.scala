package persistence

import configuration.PostgresProperties
import scalikejdbc._

import scala.concurrent.Future

class Db(config: PostgresProperties) {

  def init(): Unit = {
    Class.forName(classOf[org.postgresql.Driver].getCanonicalName)
    ConnectionPool.singleton(config.connectionString, config.user, config.password)
  }

  import slick.jdbc.PostgresProfile.api._

  private lazy val db = Database.forDriver(
    driver = new org.postgresql.Driver(),
    url = config.connectionString,
    user = config.user,
    password = config.password)

  def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = db.run(action)

}
