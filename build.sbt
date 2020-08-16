import Dependencies._

scalaVersion := "2.13.2"

name := "humidity-sensor-statistics-task"

organization := "com.jobo"

version := "1.0"

scalacOptions ++= Seq("-Ywarn-unused:_", "-Xfatal-warnings", "-deprecation")

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("com.jobo.Main")

libraryDependencies ++= Seq(
    fs2Core,
    fs2IO,
    logback,
    scalaLogging,
    scalaTest
)
