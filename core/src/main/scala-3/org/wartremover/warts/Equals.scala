package org.wartremover
package warts

object Equals extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      val methods = Seq("==", "!=", "equals", "eq", "ne")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr => // && methods.exists(m => sourceCodeContains(t, m)) =>
            t.asExpr match {
              case '{ ($x1: Any) == ($x2: Any) } =>
                error(tree.pos, "== is disabled - use === or equivalent instead")
              case '{ ($x1: Any) != ($x2: Any) } =>
                error(tree.pos, "!= is disabled - use =/= or equivalent instead")
              case '{ ($x1: Any) equals ($x2: Any) } =>
                error(tree.pos, "equals is disabled - use === or equivalent instead")
              case '{ ($x1: AnyRef) eq ($x2: AnyRef) } =>
                error(tree.pos, "eq is disabled - use === or equivalent instead")
              case '{ ($x1: AnyRef) ne ($x2: AnyRef) } =>
                error(tree.pos, "ne is disabled - use =/= or equivalent instead")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            getSyntheticPartialFunction(tree) match {
              case Some(pf) =>
                pf.body.foreach { t =>
                  traverseTree(t)(owner)
                }
              case None =>
                super.traverseTree(tree)(owner)
            }
        }
      }
    }
  }
}
