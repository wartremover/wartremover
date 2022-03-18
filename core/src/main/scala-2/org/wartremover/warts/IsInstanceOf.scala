package org.wartremover
package warts

import scala.annotation.nowarn

object IsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val IsInstanceOfName = TermName("isInstanceOf")
    val CanEqualName = TermName("canEqual")
    val EqualsName = TermName("equals")
    new u.Traverser {
      @nowarn("msg=lineContent")
      override def traverse(tree: Tree): Unit = {
        val synthetic = isSynthetic(u)(tree)
        tree match {

          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          // Ignore synthetic canEquals() and equals()
          case DefDef(_, CanEqualName | EqualsName, _, _, _, _) if synthetic =>

          // Otherwise nope, for non-synthetic receivers
          case Select(id, IsInstanceOfName)
              if !isSynthetic(u)(id)
                && tree.pos.lineContent.contains(IsInstanceOfName.toString) =>
            error(u)(tree.pos, "isInstanceOf is disabled")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}
