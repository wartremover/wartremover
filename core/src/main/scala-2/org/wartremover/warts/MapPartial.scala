package org.wartremover.warts

import org.wartremover.{WartTraverser, WartUniverse}

object MapPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val mapSymbol = rootMirror.staticClass("scala.collection.Map")
    val ApplyName = TermName("apply")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(left, ApplyName) if left.tpe.baseType(mapSymbol) != NoType =>
            error(u)(tree.pos, "Map#apply is disabled - use Map#getOrElse instead")
          case LabelDef(_, _, rhs) if isSynthetic(u)(tree) =>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
