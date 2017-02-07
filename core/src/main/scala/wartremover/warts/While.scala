package org.wartremover
package warts

object While extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case LabelDef(name, _, _)
            if !isSynthetic(u)(tree) && (name.toString.startsWith("while") || name.toString.startsWith("doWhile")) =>
            error(u)(tree.pos, "while is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
