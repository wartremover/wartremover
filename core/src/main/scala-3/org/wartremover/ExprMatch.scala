package org.wartremover

import scala.quoted.Expr
import scala.quoted.Quotes

object ExprMatch {
  // https://github.com/scala/scala/blob/0d570fbcfddd527bd9c7946dcf7af64d78efa00a/src/library/scala/PartialFunction.scala#L309-L332
  private val fallback = new String("")
  private val fallbackFunction: Any => String = _ => fallback
}

abstract class ExprMatch(
  exprMatch: Quotes ?=> PartialFunction[Expr[Any], String]
) extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            val e = t.asExpr
            val message = exprMatch.applyOrElse(e, ExprMatch.fallbackFunction)
            if (message ne ExprMatch.fallback) {
              error(tree.pos, message)
            } else {
              tree match {
                case _: Typed =>
                case _ =>
                  super.traverseTree(tree)(owner)
              }
            }
          case _: Typed =>
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
