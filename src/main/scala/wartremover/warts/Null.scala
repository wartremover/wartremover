package org.brianmckenna.wartremover
package warts

object Null extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val UnapplyName: TermName = "unapply"
    val elemSymbol = rootMirror.staticClass("scala.xml.Elem")
    new Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
        tree match {
          // Ignore xml literals
          case Apply(Select(left, _), _) if left.tpe.baseType(elemSymbol) != NoType =>
          // Ignore synthetic case class's companion object unapply
          case ModuleDef(mods, _, Template(parents, self, stats)) if synthetic =>
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
          case LabelDef(_, _, _) if synthetic =>
            // Don't check these
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
