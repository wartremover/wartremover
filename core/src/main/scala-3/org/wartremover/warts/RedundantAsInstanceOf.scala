package org.wartremover
package warts

object RedundantAsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case TypeApply(Select(x1, "asInstanceOf"), x2 :: Nil) if x1.tpe.widen =:= x2.tpe =>
            error(tree.pos, "redundant asInstanceOf")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
