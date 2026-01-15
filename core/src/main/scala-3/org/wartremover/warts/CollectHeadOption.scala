package org.wartremover
package warts

object CollectHeadOption extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "collect") || sourceCodeNotContains(tree, "headOption") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Iterable[t1]).collect($f).headOption } =>
                error(t.pos, "you can use collectFirst instead of collect.headOption")
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
