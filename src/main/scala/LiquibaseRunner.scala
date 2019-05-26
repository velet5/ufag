import java.sql.{Connection, DriverManager}

import liquibase.changelog.ChangeLogParameters
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.parser.ChangeLogParserFactory
import liquibase.resource.{ClassLoaderResourceAccessor, CompositeResourceAccessor, FileSystemResourceAccessor}
import liquibase.{Contexts, LabelExpression, Liquibase}
import old.configuration.{Configuration, PostgresProperties}
import pureconfig.generic.auto._

object LiquibaseRunner {

  def main(args: Array[String]): Unit = {
    val config = pureconfig.loadConfigOrThrow[Configuration].postgres
    runUpdate(config)
  }

  def runUpdate(config: PostgresProperties): Unit = {
    val changeLogFile = config.migrationsFile

    var conn: Connection = null
    try {
      conn = DriverManager.getConnection(config.connectionString, config.user, config.password)

      val db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))

      val changeLogParameters = new ChangeLogParameters(db)
      val resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), new FileSystemResourceAccessor())
      val parser = ChangeLogParserFactory.getInstance.getParser(changeLogFile, resourceAccessor)
      val changelog = parser.parse(changeLogFile, changeLogParameters, resourceAccessor)

      val liquibase = new Liquibase(changelog, resourceAccessor, db)
      liquibase.checkLiquibaseTables(false, changelog, new Contexts(), new LabelExpression())

      liquibase.update(migrationsTag)
    } finally {
      if (conn != null) conn.close()
    }
  }

  // private

  private val migrationsTag = "main"

}
