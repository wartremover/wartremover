package org.wartremover
package warts

import scala.annotation.nowarn

object RedundantAsInstanceOf extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser =
    new u.Traverser {
      import u.universe._
      @nowarn("msg=lineContent")
      override def traverse(tree: u.universe.Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) || isSynthetic(u)(tree) =>
          case TypeApply(Select(e, TermName("asInstanceOf")), t :: Nil)
              if (e.tpe.widen =:= t.tpe) && tree.pos.lineContent.contains("asInstanceOf") =>
            error(u)(tree.pos, "redundant asInstanceOf")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
}
