package org.wartremover
package warts

object ReverseIterator extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "iterator") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Seq[t]).reverse.iterator } =>
                error(t.pos, "you can use reverseIterator instead of reverse.iterator")
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
