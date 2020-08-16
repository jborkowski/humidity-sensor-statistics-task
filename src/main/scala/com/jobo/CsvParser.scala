package com.jobo

import com.jobo.Model.Measurement
import fs2.{Pipe, text}

import scala.util.Try

object CsvParser {
  private val NaN = "NaN"
  private val Comma = ','

  def parse[F[_]]: Pipe[F, Byte, Measurement] =
    _.through(text.utf8Decode)
      .through(text.lines)
      .filter(!_.isEmpty)
      .drop(1)
      .map(toMeasure)

  private val toMeasure: String => Measurement = _.split(Comma).toList match {
    case sensorId :: NaN :: Nil      =>
      Measurement.NaN(sensorId)
    case sensorId :: humidity :: Nil =>
      Try(humidity.toInt).map(Measurement.Ok(sensorId, _)).getOrElse(Measurement.ParseError(sensorId))
    case _ => Measurement.UnknownError
  }

}
