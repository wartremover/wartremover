package org.wartremover
package warts

object FinalVal extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case ValDef(mods, _, _, _) if mods.hasFlag(FINAL) && !mods.hasFlag(MUTABLE) && !isSynthetic(u)(tree) =>
            u.error(tree.pos, "final val is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
