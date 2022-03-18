package org.wartremover
package warts

object LeakingSealed extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: ClassDef =>
            if (
              !t.symbol.flags.is(Flags.Synthetic) &&
              t.symbol.isClassDef &&
              !t.symbol.flags.is(Flags.Final) &&
              !t.symbol.flags.is(Flags.Sealed) &&
              t.parents.collect { case x: TypeTree => x.symbol.flags }.exists(_.is(Flags.Sealed))
            ) {
              error(tree.pos, "Descendants of a sealed type must be final or sealed")
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
