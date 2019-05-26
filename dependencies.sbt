libraryDependencies ++= {

  object Version {
    val apacheCommonsLang = "3.9"
    val apacheCommonsText = "1.6"
    val akka = "2.5.19"
    val akkaHttp = "10.1.8"
    val cats = "1.6.0"
    val catsEffect = "1.3.0"
    val circe = "0.11.1"
    val httpAsyncClient = "4.1.3"
    val jackson = "2.9.5"
    val liquibase = "3.6.2"
    val log4cats = "0.3.0"
    val logback = "1.2.3"
    val manatki = "0.8.0"
    val postgres = "42.2.2"
    val pureconfig = "0.11.0"
    val sentry = "1.7.16"
    val slick = "3.2.3"
    val sttp = "1.5.17"
    val tapir = "0.7.10"
  }

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val apacheCommonsLang = "org.apache.commons" % "commons-lang3" % Version.apacheCommonsLang
  val apacheCommonsText = "org.apache.commons" % "commons-text" % Version.apacheCommonsText
  val apacheHttpAsyncClient = "org.apache.httpcomponents" % "httpasyncclient" % Version.httpAsyncClient
  val catsCore = "org.typelevel" %% "cats-core" % Version.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % Version.catsEffect
  val circeCore = "io.circe" %% "circe-core" % Version.circe
  val circeGeneric = "io.circe" %% "circe-generic" % Version.circe
  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % Version.jackson
  val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.jackson
  val liquibase = "org.liquibase" % "liquibase-core" % Version.liquibase
  val log4cats = "io.chrisdavenport" %% "log4cats-slf4j" % Version.log4cats
  val logback = "ch.qos.logback" % "logback-classic" % Version.logback
  val manatkiDerevoCore = "org.manatki" %% "derevo-core" % Version.manatki
  val manatkiDerevoCirce = "org.manatki" %% "derevo-circe" % Version.manatki
  val postgres = "org.postgresql" % "postgresql" % Version.postgres
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % Version.pureconfig
  val pureconfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.pureconfig
  val sentry = "io.sentry" % "sentry" % Version.sentry
  val sentryLogback = "io.sentry" % "sentry-logback" % Version.sentry
  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  val sttpCore = "com.softwaremill.sttp" %% "core" % Version.sttp
  val tapirCore = "com.softwaremill.tapir" %% "tapir-core" % Version.tapir
  val tapirAkkaHttpServer = "com.softwaremill.tapir" %% "tapir-akka-http-server" % Version.tapir
  val tapirJsonCirce = "com.softwaremill.tapir" %% "tapir-json-circe" % Version.tapir

  Seq(
    akkaHttp,
    akkaStream,
    apacheCommonsLang,
    apacheCommonsText,
    apacheHttpAsyncClient,
    catsCore,
    catsEffect,
    circeCore,
    circeGeneric,
    jacksonCore,
    jacksonModuleScala,
    liquibase,
    log4cats,
    logback,
    manatkiDerevoCore,
    manatkiDerevoCirce,
    postgres,
    pureconfig,
    pureconfigCatsEffect,
    sentry,
    sentryLogback,
    slick,
    sttpCore,
    tapirCore,
    tapirAkkaHttpServer,
    tapirJsonCirce,
  )

}
