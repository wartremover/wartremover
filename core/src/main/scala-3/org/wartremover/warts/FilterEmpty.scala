package org.wartremover
package warts

object FilterEmpty extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "filter") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Iterable[t1]).filter($f).isEmpty } =>
                error(t.pos, "you can use exists instead of filter.isEmpty")
              case '{ ($x: collection.Iterable[t1]).filter($f).nonEmpty } =>
                error(t.pos, "you can use exists instead of filter.nonEmpty")
              case '{ ($x: collection.Iterable[t1]).filterNot($f).isEmpty } =>
                error(t.pos, "you can use forall instead of filterNot.isEmpty")
              case '{ ($x: collection.Iterable[t1]).filterNot($f).nonEmpty } =>
                error(t.pos, "you can use forall instead of filterNot.nonEmpty")
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
