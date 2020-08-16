package com.jobo

import cats.effect.{ContextShift, IO}
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext

class ProgramSpec extends AnyFunSuite {
  test("should calculate correct stats") {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    val path = "src/test/resources/test-data"

    val expectedResult =
      """Num of processed files: 2
        |Num of processed measurements: 7
        |Num of failed measurements: 2
        |
        |Sensors with highest avg humidity:
        |
        |sensor-id,min,avg,max
        |s2,78,82.0,88
        |s1,10,54.0,98
        |s3,NaN,NaN,NaN
        |""".stripMargin

    val output = Program[IO](path, 1).process.compile.toList.unsafeRunSync()

    assert(output.contains(expectedResult))
  }
}
