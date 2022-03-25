package org.wartremover
package warts

object GetGetOrElse extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Map[t1, t2]).get($k).getOrElse($v) } =>
                error(tree.pos, "you can use Map#getOrElse(key, default) instead of get(key).getOrElse(default)")
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
