package org.wartremover
package warts

object Overloading extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: DefDef if !isSynthetic(u)(t) && (!t.mods.hasFlag(Flag.OVERRIDE) && t.symbol.overrides.isEmpty) =>
            val owner = t.symbol.owner
            if (owner.isClass && !owner.isSynthetic
                && owner.typeSignature.decls.nonEmpty
                && owner.typeSignature.members
                  .count(x => x.isMethod && !x.annotations.exists(isWartAnnotation(u)) && x.name == t.name) > 1) {
              error(u)(t.pos, "Overloading is disabled")
            }
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
