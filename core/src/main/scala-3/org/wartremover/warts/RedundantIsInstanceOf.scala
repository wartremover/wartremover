package org.wartremover
package warts

object RedundantIsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case _ if tree.isExpr =>
            tree.asExpr match {
              case '{
                    type t1
                    type t2
                    ($x: `t1`).isInstanceOf[`t2`]
                  } if TypeRepr.of[t1] <:< TypeRepr.of[t2] && sourceCodeContains(tree, "isInstanceOf") =>
                error(tree.pos, "redundant isInstanceOf")
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
