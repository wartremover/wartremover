package org.brianmckenna.wartremover
package warts

object Null extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val UnapplyName: TermName = "unapply"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          // Ignore synthetic case class's companion object unapply
          case ModuleDef(mods, _, Template(parents, self, stats)) =>
            mods.annotations foreach { annotation =>
              traverse(annotation)
            }
            parents foreach { parent =>
              traverse(parent)
            }
            traverse(self)
            stats filter {
              case DefDef(_, UnapplyName, _, _, _, _) =>
                false
              case _ =>
                true
            } foreach { stat =>
              traverse(stat)
            }
          case Literal(Constant(null)) =>
            u.error(tree.pos, "null is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
