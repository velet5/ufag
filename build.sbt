name := "ufag"

version := "0.1"

scalaVersion := "2.12.8"

organization := "velet5"

mainClass := Some("Main")

enablePlugins(DockerPlugin)

scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification",
)
