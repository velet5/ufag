package persistence

import configuration.PostgresProperties
import scalikejdbc._

class Db(config: PostgresProperties) {

  def init(): Unit = {
    Class.forName(classOf[org.postgresql.Driver].getCanonicalName)
    ConnectionPool.singleton("jdbc:postgresql://localhost:5432/ufag", config.user, config.password)
  }

}
