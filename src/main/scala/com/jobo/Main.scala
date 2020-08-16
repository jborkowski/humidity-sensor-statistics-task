package com.jobo

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.LazyLogging

object Main extends IOApp with LazyLogging {
  val MaxConcurrentStreams = 4

  def run(args: List[String]): IO[ExitCode] = {
    args.headOption.map { dirPath =>
      Program[IO](dirPath, MaxConcurrentStreams)
        .process
        .through(StatisticsPrinter.toSeparatedLines)
        .map(line => logger.info(line))
        .compile
        .drain
        .as(ExitCode.Success)
    } getOrElse IO.delay(
      logger.error("Missing argument with path to measurements directory")
    ).as(ExitCode.Error)
  }
}
