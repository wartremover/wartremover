package org.wartremover
package warts

import scala.quoted.Quotes
import scala.quoted.Type

abstract class ForbidType[A <: AnyKind](message: String)(using getType: Quotes ?=> Type[A]) extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private[this] val A: TypeRepr = TypeRepr.of[A]
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case a: TypeTree if a.tpe =:= A =>
            error(tree.pos, message)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
