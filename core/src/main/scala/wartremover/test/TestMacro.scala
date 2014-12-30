package org.brianmckenna.wartremover
package test

import language.experimental.macros
import reflect.macros.blackbox.Context

object WartTestTraverser {
  case class Result(errors: List[String], warnings: List[String])

  def apply(t: WartTraverser)(a: Any): Result = macro applyImpl
  def applyImpl(c: Context)(t: c.Expr[WartTraverser])(a: c.Expr[Any]) = {
    import c.universe._

    val traverser = c.eval[WartTraverser](c.Expr(c.untypecheck(t.tree.duplicate)))

    var errors = collection.mutable.ListBuffer[String]()
    var warnings = collection.mutable.ListBuffer[String]()

    object MacroTestUniverse extends WartUniverse {
      val universe: c.universe.type = c.universe
      def error(pos: universe.Position, message: String) = errors += message
      def warning(pos: universe.Position, message: String) = warnings += message
    }

    traverser(MacroTestUniverse).traverse(a.tree)

    c.Expr(q"WartTestTraverser.Result(List(..${errors.toList}), List(..${warnings.toList}))")
  }
}
