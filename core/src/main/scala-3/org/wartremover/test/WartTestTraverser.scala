package org.wartremover.test

import org.wartremover.LogLevel
import org.wartremover.WartTraverser
import org.wartremover.WartUniverse
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.TypeTree
import dotty.tools.dotc.ast.tpd.TreeTraverser
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.quoted.PickledQuotes
import dotty.tools.dotc.core.Contexts.FreshContext
import dotty.tools.dotc.reporting.Diagnostic
import dotty.tools.dotc.reporting.Reporter
import dotty.tools.dotc.interfaces.Diagnostic as DiagnosticInterface
import scala.collection.mutable.ListBuffer
import scala.quoted.Expr
import scala.quoted.FromExpr
import scala.quoted.Quotes
import scala.quoted.ToExpr
import scala.quoted.Type
import scala.quoted.runtime.impl.ExprImpl
import scala.quoted.runtime.impl.QuotesImpl
import scala.reflect.NameTransformer

class WartReporter extends Reporter {
  private[this] val lock = new Object
  private[this] val values = ListBuffer.empty[Diagnostic]
  override def doReport(diagnostic: Diagnostic)(using Context): Unit = {
    lock.synchronized {
      values += diagnostic
    }
  }

  def result: List[Diagnostic] = lock.synchronized {
    values.result()
  }
}

object WartTestTraverser {
  case class Result(errors: List[String], warnings: List[String])
  object Result {
    implicit val toExprInstance: ToExpr[Result] =
      new ToExpr[Result] {
        override def apply(x: Result)(using Quotes) = '{
          Result(
            errors = ${ Expr(x.errors) },
            warnings = ${ Expr(x.warnings) }
          )
        }
      }
  }

  inline def apply[A <: WartTraverser](inline t: A)(inline a: Any): Result = ${ applyImpl[A]('t, 'a) }

  private[this] def applyImpl[A <: WartTraverser: Type](t: Expr[A], expr: Expr[Any])(using q1: Quotes): Expr[Result] = {
    val q2 = q1.asInstanceOf[QuotesImpl]
    val reporter = new WartReporter
    q2.ctx.asInstanceOf[FreshContext].setReporter(reporter)
    val wart = {
      val name = q1.reflect.TypeRepr.of[A].show
      val clazz = Class.forName(name + NameTransformer.MODULE_SUFFIX_STRING)
      clazz.getField(NameTransformer.MODULE_INSTANCE_NAME).get(null).asInstanceOf[WartTraverser]
    }
    val universe = new WartUniverse(
      onlyWarning = false,
      logLevel = LogLevel.Info,
      quotes = q1
    )
    val x = wart.apply(universe)
    val term = x.q.reflect.asTerm(expr)
    x.traverseTree(term)(term.symbol)
    val result1 = reporter.result
    val warnings = result1.collect { case a if a.level() == DiagnosticInterface.WARNING => a.message() }
    val errors = result1.collect { case a if a.level() == DiagnosticInterface.ERROR => a.message() }
    Expr(Result(errors = errors, warnings = warnings))
  }
}
