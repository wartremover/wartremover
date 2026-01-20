package org.wartremover
package warts

object IsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "isInstanceOf") =>
          case t if hasWartAnnotation(t) =>
          case _: Typed =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: t1).isInstanceOf[t2] } =>
                error(tree.pos, "isInstanceOf is disabled")
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
