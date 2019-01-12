package persistence

import configuration.PostgresProperties
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class Db(config: PostgresProperties) {

  def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = db.run(action)

  // private

  private lazy val db = Database.forDriver(
    driver = new org.postgresql.Driver(),
    url = config.connectionString,
    user = config.user,
    password = config.password)

}
