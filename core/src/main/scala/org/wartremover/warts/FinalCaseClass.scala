package org.wartremover
package warts

object FinalCaseClass extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case ClassDef(mods, _, _, _) if mods.hasFlag(CASE) && !mods.hasFlag(FINAL | SEALED) =>
            error(u)(tree.pos, "case classes must be final")
          // Do not look inside other classes.
          // See: https://groups.google.com/forum/#!msg/scala-internals/vw8Kek4zlZ8/LAeakfeR3RoJ
          case ClassDef(_, _, _, _) =>
          case t => super.traverse(tree)
        }
      }
    }
  }
}
