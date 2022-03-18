package org.wartremover
package warts

object ImplicitConversion extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: DefDef
            if t.symbol.isImplicit && t.symbol.isPublic && t.vparamss.flatten.exists(x => !x.symbol.isImplicit) && !isSynthetic(u)(t) =>
            error(u)(tree.pos, "Implicit conversion is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
