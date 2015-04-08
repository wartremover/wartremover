package org.brianmckenna.wartremover
package warts

object Throw extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val ProductElementName: TermName = "productElement"
    new u.Traverser {
      override def traverse(tree: Tree) {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case dd@DefDef(_, ProductElementName , _, _, _, _) if isSynthetic(u)(dd) =>
          case u.universe.Throw(_) =>
            u.error(tree.pos, "throw is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
