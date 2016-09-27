name := """actor-streams"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.8" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.4.8"
