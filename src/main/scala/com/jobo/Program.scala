package com.jobo

import java.io.{File, FilenameFilter}
import java.nio.file.Path

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits.catsKernelStdMonoidForMap
import com.jobo.Model.Measurement
import fs2.{Stream, io}

final case class Program[F[_]: Concurrent: ContextShift](
    directoryPath: String,
    maxConcurrentStreams: Int
  ) {

  private val csvFileFilter: FilenameFilter =
    (_, name: String) => name.toLowerCase.endsWith(".csv")

  private val paths: List[Path] = Option(new File(directoryPath).listFiles(csvFileFilter)).toList.flatten.map(_.toPath)

  val process: Stream[F, String] = {
    val streams = for {
      path    <- Stream.emits(paths)
      blocker <- Stream.resource(Blocker[F])
      streams = io.file.readAll(path, blocker, 4096)
        .through(CsvParser.parse)
        .foldMap(Measurement.toStats)
    } yield streams

    streams
      .parJoin(maxConcurrentStreams)
      .foldMonoid
      .map(StatisticsPrinter.formatOutput(_, paths.size))
  }
}
