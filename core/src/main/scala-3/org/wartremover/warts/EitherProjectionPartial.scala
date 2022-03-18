package org.wartremover
package warts

import scala.annotation.nowarn

@nowarn("msg=right-biased")
@nowarn("msg=Either.toOption.get")
@nowarn("msg=Either.swap.getOrElse")
object EitherProjectionPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ (${ a }: Either.RightProjection[left, right]).get } =>
                error(t.pos, "RightProjection#get is disabled - use RightProjection#toOption instead")
              case '{ (${ a }: Either.LeftProjection[left, right]).get } =>
                error(t.pos, "LeftProjection#get is disabled - use LeftProjection#toOption instead")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
