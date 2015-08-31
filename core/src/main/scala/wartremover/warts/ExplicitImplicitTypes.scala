package org.brianmckenna.wartremover
package warts

import scala.util.matching.Regex

object ExplicitImplicitTypes extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    def hasTypeAscription(tree: ValOrDefDef) : Boolean = 
      new Regex("""(val|def)\s*""" + tree.name.decodedName.toString.trim + """(\[.*\])?(\(.*\))*\s*:""")
        .findFirstIn(tree.pos.lineContent).nonEmpty

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: ValOrDefDef if t.mods.hasFlag(IMPLICIT) && !t.mods.hasFlag(PARAM) && !t.mods.hasFlag(LOCAL) && !isSynthetic(u)(t) && !hasTypeAscription(t) =>
            u.error(tree.pos, "implicit definitions must have an explicit type ascription")
          case t => super.traverse(tree)
        }
      }
    }
  }
}
