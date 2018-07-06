name := "ufag"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.5",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.2.2",
  "org.postgresql" % "postgresql" % "42.2.2",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "com.github.pureconfig" %% "pureconfig" % "0.9.1"
)