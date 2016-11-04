package org.wartremover
package warts

object UnsafeInheritance extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    def overridableImplementation(t: DefDef) =
      !t.mods.hasFlag(FINAL | DEFERRED | PRIVATE) && !t.symbol.owner.isModuleClass && t.symbol.owner.isClass && {
        val cls = t.symbol.owner.asClass
        !cls.isFinal && !cls.isSealed && !cls.isPrivate && !cls.isProtected
      }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: DefDef if !isSynthetic(u)(t)
              && !t.symbol.asMethod.isConstructor
              && !t.symbol.asMethod.isAccessor
              && overridableImplementation(t) =>
            u.error(t.pos, "Method must be final or abstract")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
