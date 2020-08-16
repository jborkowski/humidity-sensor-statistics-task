package com.jobo

import cats.kernel.Semigroup
import cats.syntax.semigroup._
import com.typesafe.scalalogging.LazyLogging

object Model extends LazyLogging {

  type SensorId = String

  sealed abstract class Measurement
  object Measurement {
    case class Ok(sensorId: SensorId, humidity: Int) extends Measurement
    case class NaN(sensorId: SensorId) extends Measurement
    case class ParseError(sensorId: SensorId) extends Measurement
    case object UnknownError extends Measurement

    val toStats: Measurement => Map[SensorId, Statistics] = {
      case Measurement.Ok(sensorId, humidity) =>
        Map(sensorId -> Statistics(Some(Sample(humidity)), 0, 1))
      case Measurement.NaN(sensorId) =>
        Map(sensorId -> Statistics(None, 1, 1))
      case Measurement.ParseError(sensorId) =>
        logger.error("Cannot parse sensor: {} value as number", sensorId)
        Map.empty
      case Measurement.UnknownError =>
        logger.error("Unsupported error occur during parsing CSV file")
        Map.empty
    }
  }

  case class Statistics(
    sample: Option[Sample],
    failed: Int,
    total: Int
  )

  object Statistics {
    implicit val semigroup: Semigroup[Statistics] = {
      case (Statistics(Some(Sample(leftMin, leftMax, leftAvg, leftValue)), leftFailed, leftTotal),
            Statistics(Some(Sample(rightMin, rightMax, rightAvg, rightValue)), rightFailed, rightTotal)) =>
        val min = math.min(leftMin.min(rightMin), math.min(leftValue, rightValue))
        val max = math.max(leftMax.max(rightMax), math.max(leftValue, rightValue))
        val leftOkays = leftTotal - leftFailed
        val rightOkays = rightTotal - rightFailed
        val avg = (leftOkays * leftAvg + rightOkays * rightAvg) / (leftOkays + rightOkays)
        val sample = Sample(min, max, avg, rightValue)
        Statistics(Some(sample), leftFailed + rightFailed, leftTotal + rightTotal)
      case (Statistics(leftSample, leftFailed, leftTotal), Statistics(rightSample, rightFailed, rightTotal)) =>
        implicit def semigroup[A]: Semigroup[Option[A]] = _.orElse(_)
        Statistics(leftSample combine rightSample, leftFailed + rightFailed, leftTotal + rightTotal)
    }
  }

  case class Sample(
    min: Int,
    max: Int,
    avg: Float,
    value: Int
  )

  object Sample {
    def apply(value: Int): Sample = new Sample(value, value, value.toFloat, value)
  }

}
