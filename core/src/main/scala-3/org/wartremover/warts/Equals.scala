package org.wartremover
package warts

object Equals extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: DefDef if (t.name == "equals") && t.symbol.flags.is(Flags.Synthetic) =>
          case Apply(Select(_, method), _ :: Nil) =>
            method match {
              case "==" =>
                error(tree.pos, "== is disabled - use === or equivalent instead")
              case "!=" =>
                error(tree.pos, "!= is disabled - use =/= or equivalent instead")
              case "equals" =>
                error(tree.pos, "equals is disabled - use === or equivalent instead")
              case "eq" =>
                error(tree.pos, "eq is disabled - use === or equivalent instead")
              case "ne" =>
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
