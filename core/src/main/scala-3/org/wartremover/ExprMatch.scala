package org.wartremover

import scala.quoted.Expr
import scala.quoted.Quotes

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
            if (exprMatch.isDefinedAt(e)) {
              val message = exprMatch.apply(e)
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
