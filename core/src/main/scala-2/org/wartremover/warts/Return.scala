package org.wartremover
package warts

object Return extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case u.universe.Return(_) =>
            error(u)(tree.pos, "return is disabled")
          case _ => super.traverse(tree)
        }
      }
    }
  }
}
