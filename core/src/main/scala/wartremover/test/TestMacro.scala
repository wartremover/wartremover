package org.wartremover
package test

import language.experimental.macros
import reflect.internal.util.Position
import reflect.macros.Context
import tools.nsc.reporters.Reporter

object WartTestTraverser {
  case class Result(errors: List[String], warnings: List[String])

  def apply(t: WartTraverser)(a: Any): Result = macro applyImpl
  def applyImpl(c: Context)(t: c.Expr[WartTraverser])(a: c.Expr[Any]) = {
    import c.universe._

    val traverser = c.eval[WartTraverser](c.Expr(c.resetLocalAttrs(t.tree.duplicate)))

    var errors = collection.mutable.ListBuffer[String]()
    var warnings = collection.mutable.ListBuffer[String]()

    object MacroTestUniverse extends WartUniverse(traverser) {
      val universe: c.universe.type = c.universe
      def error(pos: universe.Position, message: String) = errors += decorateMessage(message)
      def warning(pos: universe.Position, message: String) = warnings += decorateMessage(message)
    }

    traverser(MacroTestUniverse).traverse(a.tree)

    c.Expr(q"WartTestTraverser.Result(List(..${errors.toList}), List(..${warnings.toList}))")
  }

  /**
   * Example:
   * <pre>{@code
   * val result = WartTestTraverser.applyToFiles(Return)(
   *   "return/A.scala",
   *   "return/B.scala"
   * )
   * }</pre>
   *
   * @param t traverser to test
   * @param paths test source files located in {@code core/src/test/resources/}
   * @return result of applying {@code t} to files at {@code paths}
   */
  def applyToFiles(t: WartTraverser)(paths: String*): Result = {
    var errors = List[String]()
    var warnings = List[String]()
    Main.compile(Main.WartArgs(List(t.className), paths.map("core/src/test/resources/" + _).toList), Some(new Reporter {
      override protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean) = severity match {
        case ERROR => errors ::= msg
        case WARNING => warnings ::= msg
        case _ =>
      }
    }))
    Result(errors, warnings)
  }
}
