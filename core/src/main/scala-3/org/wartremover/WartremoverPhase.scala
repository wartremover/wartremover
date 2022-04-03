package org.wartremover

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Symbols.defn
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.quoted.PickledQuotes
import dotty.tools.dotc.quoted.QuotesCache
import dotty.tools.dotc.typer.TyperPhase
import dotty.tools.dotc.report
import java.util.concurrent.atomic.AtomicBoolean
import scala.quoted.Quotes
import scala.util.control.NonFatal
import scala.reflect.NameTransformer
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class WartremoverPhase(
  errorWarts: List[WartTraverser],
  warningWarts: List[WartTraverser],
  loadFailureWarts: List[(String, Throwable)],
  excluded: List[String],
  logLevel: LogLevel,
  initialLog: AtomicBoolean,
  parallel: Boolean,
) extends PluginPhase {
  override def phaseName = "wartremover"

  override def run(using c: Context): Unit = {
    logLevel match {
      case LogLevel.Info | LogLevel.Debug =>
        if (initialLog.getAndSet(false)) {
          if (errorWarts.nonEmpty) {
            report.echo("error warts = " + errorWarts.map(_.getClass.getName.dropRight(1)).mkString(", "))
          }
          if (warningWarts.nonEmpty) {
            report.echo("warning warts = " + warningWarts.map(_.getClass.getName.dropRight(1)).mkString(", "))
          }
          if (loadFailureWarts.nonEmpty) {
            report.warning(s"load failure warts = " + loadFailureWarts.mkString(", "))
          }
          if (excluded.nonEmpty) {
            report.echo("excluded = " + excluded.mkString(", "))
          }
        }
      case LogLevel.Disable =>
    }
    val skip = excluded.exists(c.source.file.absolute.path.startsWith)
    logLevel match {
      case LogLevel.Info | LogLevel.Disable =>
      case LogLevel.Debug =>
        if (skip) {
          report.echo("skip wartremover " + c.compilationUnit.source.file.toString)
        } else {
          report.echo("run wartremover " + c.compilationUnit.source.file.toString)
        }
    }
    if (!skip) {
      super.run
    }
  }

  override val runsAfter = Set(TyperPhase.name)

  override def prepareForUnit(tree: tpd.Tree)(using c: Context): Context = {
    if (parallel) {
      Future(
        run(tree)
      )(ExecutionContext.global)
      c
    } else {
      run(tree)
    }
  }

  private[this] def run(tree: tpd.Tree)(using c: Context): Context = {
    val c2 = QuotesCache.init(c.fresh)
    val q = scala.quoted.runtime.impl.QuotesImpl()(using c2)
    def runWart(w: WartTraverser, onlyWarning: Boolean): Unit = {
      val universe = WartUniverse(
        onlyWarning = onlyWarning,
        logLevel = logLevel,
        quotes = q,
      )
      val traverser = w.apply(universe)
      val t = tree.asInstanceOf[traverser.q.reflect.Tree]
      try {
        traverser.traverseTree(t)(t.symbol)
      } catch {
        case NonFatal(e) =>
          logLevel match {
            case LogLevel.Disable =>
            case LogLevel.Info | LogLevel.Debug =>
              report.warning(e.toString, tree.srcPos)
          }
      }
    }

    errorWarts.foreach(w => runWart(w = w, onlyWarning = false))
    warningWarts.foreach(w => runWart(w = w, onlyWarning = true))
    c
  }

}
