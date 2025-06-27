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
          case t @ ValDef(mods, _, _, _)
              if mods.hasFlag(FINAL) &&
                !mods.hasFlag(MUTABLE) &&
                !hasTypeAscription(u)(t) &&
                !isSynthetic(u)(tree) &&
                PartialFunction.cond(t.tpt) { case tp @ TypeTree() =>
                  // https://scala-lang.org/files/archive/spec/2.13/06-expressions.html#constant-expressions
                  PartialFunction.cond(tp.tpe) { case ConstantType(_) =>
                    true
                  }
                } =>
            error(u)(tree.pos, "final val is disabled - use non-final val or final def or add type ascription")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
