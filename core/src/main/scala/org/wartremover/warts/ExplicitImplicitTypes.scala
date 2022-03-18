package org.wartremover
package warts

object ExplicitImplicitTypes extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: ValOrDefDef if t.mods.hasFlag(IMPLICIT) && !t.mods.hasFlag(PARAM) && !isPrivate(u)(t)
              && !isSynthetic(u)(t) && !hasTypeAscription(u)(t) =>
            error(u)(tree.pos, "implicit definitions must have an explicit type ascription")
          case t => super.traverse(tree)
        }
      }
    }
  }
}
