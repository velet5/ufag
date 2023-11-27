import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = "Application"
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString =
    classpath.files.map("/app/" + _.getName).mkString(":") + ":" + jarTarget

  new Dockerfile {
    // Base image
    from("alpine:3.18.4")
    // Install Java
    runRaw("apk add --no-cache openjdk17-jre-headless")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint(
      "java",
      "-Dconfig.file=application.conf",
      "-cp",
      classpathString,
      mainclass
    )
  }
}

imageNames in docker := {
  val now = LocalDateTime.now()
  val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
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
