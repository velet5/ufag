import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

name := "ufag"

version := "0.1"

scalaVersion := "2.12.6"

organization := "velet5"

mainClass := Some("Application")

enablePlugins(DockerPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.5",
  "org.postgresql" % "postgresql" % "42.2.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "org.apache.commons" % "commons-text" % "1.4",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.liquibase" % "liquibase-core" % "3.6.2",
  "com.typesafe.slick" %% "slick" % "3.2.3"
)

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = "Application"
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName).mkString(":") + ":" + jarTarget

  new Dockerfile {
    // Base image
    from("andreptb/oracle-java:8") // fixme migrate to own docker image
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

imageNames in docker := {
  val now = LocalDateTime.now()
  val formatter = DateTimeFormatter.ofPattern("yyyymmdd-HHMMss")
  val tagValue = formatter.format(now)

  Seq(
    // Sets the latest tag
    ImageName(s"${organization.value}/${name.value}:latest"),

    // Sets a name with a tag that contains the project version
    ImageName(
      namespace = Some(organization.value),
      repository = name.value,
      tag = Some(tagValue)
    )
  )
}