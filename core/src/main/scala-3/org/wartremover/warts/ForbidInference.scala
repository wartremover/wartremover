package org.wartremover
package warts

import dotty.tools.dotc.ast.tpd.InferredTypeTree
import scala.annotation.nowarn
import scala.quoted.Quotes
import scala.quoted.Type

abstract class ForbidInference[A](using getType: Quotes ?=> Type[A]) extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val A: TypeRepr = TypeRepr.of[A]
      @nowarn("msg=cannot be checked at runtime")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case a: Inferred
              if a.tpe =:= A && a.isInstanceOf[InferredTypeTree] && !a.symbol.flags.is(Flags.JavaDefined) =>
            val name = A.show.split('.').last // TODO more better way?
            error(tree.pos, s"Inferred type containing ${name}: ${name}")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
