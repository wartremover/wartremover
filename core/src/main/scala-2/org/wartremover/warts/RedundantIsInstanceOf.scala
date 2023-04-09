package org.wartremover
package warts

import scala.annotation.nowarn

object RedundantIsInstanceOf extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser =
    new u.Traverser {
      import u.universe._
      @nowarn("msg=lineContent")
      override def traverse(tree: u.universe.Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) || isSynthetic(u)(tree) =>
          case TypeApply(Select(e, TermName("isInstanceOf")), t :: Nil)
              if (e.tpe <:< t.tpe) && tree.pos.lineContent.contains("isInstanceOf") =>
            error(u)(tree.pos, "redundant isInstanceOf")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
}
