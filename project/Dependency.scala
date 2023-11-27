import sbt.*
import sbt.Keys.*

object Dependency {

  object Version {
    val circe = "0.14.6"
    val commonsLang3 = "3.13.0"
    val http4s = "0.23.15"
    val liquibase = "4.24.0"
    val logback = "1.4.11"
    val pureconfig = "0.17.4"
    val quill = "4.8.0"
    val sttp = "3.9.0"
    val tapir = "1.8.0"
    val zio = "2.0.18"
    val zioInteropCats = "23.1.0.0"
  }

  val compile: Seq[ModuleID] = Vector(
    // Circe Generic
    "io.circe" %% "circe-generic" % Version.circe,
    // Circe Parser
    "io.circe" %% "circe-parser" % Version.circe,
    // Commons Lang
    "org.apache.commons" % "commons-lang3" % Version.commonsLang3,
    // Http4s Blaze Server
    "org.http4s" %% "http4s-blaze-server" % Version.http4s,
    // Liquibase Core
    "org.liquibase" % "liquibase-core" % Version.liquibase,
    // Logback Classic
    "ch.qos.logback" % "logback-classic" % Version.logback,
    // PureConfig Core
    "com.github.pureconfig" %% "pureconfig-core" % Version.pureconfig,
    // PureConfig CatsEffect
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.pureconfig,
    // Quill Jdbc ZIO
    "io.getquill" %% "quill-jdbc-zio" % Version.quill,
    // Sttp Core
    "com.softwaremill.sttp.client3" %% "core" % Version.sttp,
    // Sttp Circe
    "com.softwaremill.sttp.client3" %% "circe" % Version.sttp,
    // Sttp Fs2
    "com.softwaremill.sttp.client3" %% "fs2" % Version.sttp,
    // Tapir Http4s Server
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Version.tapir,
    // Tapir JSON Circe
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Version.tapir,
    // Tapir Sttp Client
    "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % Version.tapir,
    // ZIO
    "dev.zio" %% "zio" % Version.zio,
    // ZIO Interop Cats
    "dev.zio" %% "zio-interop-cats" % Version.zioInteropCats
  )

  val test: Seq[ModuleID] = Vector()

}
