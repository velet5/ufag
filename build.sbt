name := "ufag"

version := "0.1"

scalaVersion := "2.12.6"

mainClass := Some("Application")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.5",
  "org.scalikejdbc" %% "scalikejdbc" % "3.2.2",
  "org.postgresql" % "postgresql" % "42.2.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "org.apache.commons" % "commons-text" % "1.4",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.liquibase" % "liquibase-core" % "3.6.2",
  "com.typesafe.slick" %% "slick" % "3.2.3"
)