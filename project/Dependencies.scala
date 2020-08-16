import sbt.stringToOrganization
object Dependencies {
  val fs2Core = "co.fs2" %% "fs2-core" % "2.4.2"
  val fs2IO = "co.fs2" %% "fs2-io" % "2.4.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0" % "test"
}
