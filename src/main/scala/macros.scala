package org.brianmckenna.wartremover

import language.experimental.{ macros => scalaMacros }
import reflect.macros.Context

object macros {
  def safe[A](expr: A) = macro safeImpl[A]

  def safeImpl[A](c: Context)(expr: c.Expr[A]): c.Expr[A] = {
    import c.universe._

    def isIgnoredStatement(tree: Tree) = tree match {
      // Scala creates synthetic blocks with <init> calls on classes.
      // The calls return Object so we need to ignore them.
      case Apply(Select(_, nme.CONSTRUCTOR), _) => true
      case _ => false
    }

    object NoUnit extends Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Block(statements, _) =>
            statements.foreach { stat =>
              val unitLike = stat.tpe =:= typeOf[Unit] || stat.isDef || isIgnoredStatement(stat)
              if (!unitLike)
                c.error(stat.pos, "Statements must return Unit")
            }
          case _ =>
        }
        super.traverse(tree)
      }
    }
    NoUnit.traverse(expr.tree)

    expr
  }
}
