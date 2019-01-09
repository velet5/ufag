package persistence

import configuration.PostgresProperties
import scalikejdbc._

class Db(config: PostgresProperties) {

  def init(): Unit = {
    Class.forName(classOf[org.postgresql.Driver].getCanonicalName)
    ConnectionPool.singleton(config.connectionString, config.user, config.password)
  }

}
