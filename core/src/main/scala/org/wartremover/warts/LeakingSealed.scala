package org.wartremover
package warts

object LeakingSealed extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t @ ClassDef(mods, _, _, _)
            if !mods.hasFlag(FINAL) && !mods.hasFlag(SEALED) && t.symbol.asClass.baseClasses.exists(_.asClass.isSealed) =>
            error(u)(tree.pos, "Descendants of a sealed type must be final or sealed")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
