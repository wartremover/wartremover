package org.wartremover.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.wartremover.LogLevel.Disable
import scala.quoted.Quotes
import scala.tasty.inspector.Inspector
import scala.tasty.inspector.Tasty
import scala.tasty.inspector.TastyInspector

object WartremoverBenchmark {
  val compilerJarPath: String =
    dotty.tools.dotc.Main.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.toURL.getFile
}

abstract class WartremoverBenchmark {
  protected def wart: org.wartremover.WartTraverser

  @Benchmark
  def test(): Int = {
    var count = 0
    val inspector: Inspector = new Inspector {
      override def inspect(using q: Quotes)(tastys: List[Tasty[q.type]]): Unit = {
        val universe = new org.wartremover.WartUniverse(true, Disable) {
          override type Q = q.type
          override val quotes: Q = q
          override protected def onWarn(msg: String, pos: quotes.reflect.Position): Unit = {
            count += 1
          }
        }
        val traverser = wart.apply(universe)
        tastys.foreach { t =>
          traverser.traverseTree(t.ast)(q.reflect.Symbol.spliceOwner)
        }
      }
    }
    TastyInspector.inspectTastyFilesInJar(WartremoverBenchmark.compilerJarPath)(inspector)
    count
  }
}
