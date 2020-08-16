package com.jobo

import cats.Show
import cats.effect.Sync
import cats.syntax.show._
import cats.syntax.foldable.toFoldableOps
import cats.instances.list.catsStdInstancesForList
import cats.implicits.catsKernelStdGroupForInt
import com.jobo.Model.{SensorId, Statistics}
import fs2.{Pipe, Stream}

object StatisticsPrinter {
  private def outputTemplate(
    numOfProcessedFiles: Int,
    numOfProcessedMeasurements: Int,
    numOfProcessedFailedMeasurements: Int,
    sortedSensors: Seq[(SensorId,Statistics)]
  ): String = {
    val sb = new StringBuilder
    sb.append(s"Num of processed files: $numOfProcessedFiles\n")
    sb.append(s"Num of processed measurements: $numOfProcessedMeasurements\n")
    sb.append(s"Num of failed measurements: $numOfProcessedFailedMeasurements\n")
    sb.append('\n')
    sb.append("Sensors with highest avg humidity:\n")
    sb.append('\n')
    if (sortedSensors.isEmpty) {
      sb.append("No data available...\n")
    } else {
      sb.append("sensor-id,min,avg,max\n")
      sortedSensors.foreach(stats => sb.append(stats.show))
    }

    sb.toString
  }

  implicit val showTupleSensorIdStats: Show[(SensorId, Statistics)] = {
    case (sensorId, Statistics(Some(sample), _, _)) => s"$sensorId,${sample.min},${sample.avg},${sample.max}\n"
    case (sensorId, Statistics(None, _, _))         => s"$sensorId,NaN,NaN,NaN\n"
  }


  def formatOutput(stats: Map[SensorId, Statistics], numOfProcessedFiles: Int): String = {
    val statsList = stats.values.toList
    val numOfProcessedMeasurements = statsList.foldMap(_.total)
    val numOfProcessedFailedMeasurements = statsList.foldMap(_.failed)
    val sortedSensors: Seq[(SensorId,Statistics)] = stats.toSeq.sortBy(_._2.sample.fold(Float.MinValue)(_.avg))(Ordering[Float].reverse)

    outputTemplate(numOfProcessedFiles, numOfProcessedMeasurements, numOfProcessedFailedMeasurements, sortedSensors)
  }

  def toSeparatedLines[F[_]: Sync]: Pipe[F, String, String] = _.flatMap(out => Stream.fromIterator(out.linesIterator))
}
