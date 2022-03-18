package org.wartremover
package warts

object Return extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: Return =>
            error(tree.pos, "return is disabled")
          case _ => super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
