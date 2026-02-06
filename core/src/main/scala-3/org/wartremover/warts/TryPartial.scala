package org.wartremover
package warts

object TryPartial extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "get") =>
          case t if hasWartAnnotation(t) =>
          case t: Select if t.isExpr =>
            t.asExpr match {
              case '{ ($x: scala.util.Try[?]).get } =>
                error(selectNamePosition(t), "Try#get is disabled")
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
