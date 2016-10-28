package org.wartremover
package warts

object LeakingSealed extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t @ ClassDef(mods, _, _, _)
            if t.symbol.asClass.baseClasses.exists { parent =>
              parent.asClass.isSealed && parent.associatedFile != t.symbol.associatedFile
            } =>
              u.error(tree.pos, "Descendants of a sealed parent must be located in the parent's file")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
