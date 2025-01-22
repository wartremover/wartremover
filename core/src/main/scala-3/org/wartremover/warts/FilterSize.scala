package org.wartremover
package warts

object FilterSize extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "filter") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Iterable[t1]).filter($f).size } =>
                error(t.pos, "you can use count instead of filter.size")
              case '{ ($x: collection.Seq[t1]).filter($f).length } =>
                error(t.pos, "you can use count instead of filter.length")
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
