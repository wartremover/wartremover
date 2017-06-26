package org.wartremover
package warts

object MissingOverride extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: DefDef
            if t.symbol.allOverriddenSymbols.nonEmpty && !t.mods.hasFlag(Flag.OVERRIDE) && !isSynthetic(u)(t) =>
            error(u)(tree.pos, "Method must have override modifier")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
