import sbt._
import sbt.Keys._

object AggregatesourcescalaBuild extends Build {

  lazy val AggregateSource = Project(
    id = "aggregate-source-scala",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "aggregate-source-scala",
      organization := "com.blogspot.marcinderylo",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(Dependencies.scalatest)
    )
  )

  object Dependencies {
    val scalatest = "org.scalatest" % "scalatest_2.10.0-RC5" % "2.0.M5-B1" % "test"
  }
}
