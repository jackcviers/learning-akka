name := "learning-akka"

version := "0.0.1"

scalaVersion := "2.10.2"

version in ThisBuild := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test",
    "org.specs2" %% "specs2" % "2.1.1" % "test"
  )

scalacOptions ++= Seq("-feature","-J-Xss6M")

