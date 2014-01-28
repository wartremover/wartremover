package org.brianmckenna.wartremover
package warts

object IsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val IsInstanceOfName: TermName = "isInstanceOf"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case u.universe.Select(_, IsInstanceOfName) =>
            u.error(tree.pos, "isInstanceOf is disabled")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
