package org.wartremover

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.LongAdder
import scala.collection.concurrent.TrieMap

private[wartremover] object Profile {
  def report(values: TrieMap[String, LongAdder], path: Option[String], logLevel: LogLevel): Unit = {
    path.withFilter(_ => values.nonEmpty).foreach { profileFilePath =>
      val xs = values.map { case (k, v) => (k, v.sum) }.toSeq
      val sum = xs.map(_._2).sum
      def toMillSeconds(n: Long): String = (n / 1000000.0).toString
      val result =
        xs.sortBy(_._2)(using implicitly[Ordering[Long]].reverse)
          .iterator
          .map { case (k, v) => s"$k ${toMillSeconds(v)} ${"%.6f".format((v.toDouble / sum) * 100)}" }
          .mkString(s"all ${toMillSeconds(sum)}\n", "\n", "\n")

      logLevel match {
        case LogLevel.Debug =>
          println(result)
        case _ =>
      }

      Files.write(
        Paths.get(profileFilePath),
        result.getBytes(StandardCharsets.UTF_8)
      )
    }
  }
}
