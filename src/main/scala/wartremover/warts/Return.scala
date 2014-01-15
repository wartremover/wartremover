package org.brianmckenna.wartremover
package warts

object Return extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case u.universe.Return(_) =>
            u.error(tree.pos, "return is disabled")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
