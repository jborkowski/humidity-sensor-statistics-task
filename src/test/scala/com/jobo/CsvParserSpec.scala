package com.jobo

import fs2.Stream
import cats.Id
import com.jobo.Model.Measurement
import org.scalatest.funsuite.AnyFunSuite

class CsvParserSpec extends AnyFunSuite {
  test("should parse valid csv") {
    val valid =
      """
        |sensor-id,humidity
        |s1,10
        |s2,88
        |s1,NaN
        |""".stripMargin

    val outputList = Stream.emits(valid.getBytes).through(CsvParser.parse[Id]).compile.toList

    val expectedResult = List(
      Measurement.Ok("s1", 10),
      Measurement.Ok("s2", 88),
      Measurement.NaN("s1")
    )
    assert(outputList == expectedResult)
  }

  test("should handle corrupted csv") {
    val corrupted =
      """
        |sensor-id,humidity
        |s1,
        |s2,88
        |s1,InvalidNumber
        |""".stripMargin

    val outputList = Stream.emits(corrupted.getBytes).through(CsvParser.parse[Id]).compile.toList

    val expectedResult = List(
      Measurement.UnknownError,
      Measurement.Ok("s2", 88),
      Measurement.ParseError("s1")
    )
    assert(outputList == expectedResult)
  }
}
