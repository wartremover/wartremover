package org.brianmckenna.wartremover
package warts

object AsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val AsInstanceOfName: TermName = "asInstanceOf"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case u.universe.Select(_, AsInstanceOfName) =>
            u.error(tree.pos, "asInstanceOf is disabled")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
