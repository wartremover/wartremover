package org.wartremover
package warts

object RedundantAsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case _ if tree.isExpr && sourceCodeContains(tree, "asInstanceOf") =>
            tree.asExpr match {
              case '{
                    type t1
                    type t2
                    ($x: `t1`).asInstanceOf[`t2`]
                  } if TypeRepr.of[t1].widen =:= TypeRepr.of[t2] =>
                error(tree.pos, "redundant asInstanceOf")
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
