package org.brianmckenna.wartremover
package warts

object Null extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val UnapplyName: TermName = "unapply"
    val xmlSymbols = (classOf[scala.xml.Elem]
      :: classOf[scala.xml.NamespaceBinding]
      :: Nil) map (c => rootMirror.staticClass(c.getCanonicalName))
    new Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
        tree match {
          // Ignore xml literals
          case Apply(Select(left, _), _) if xmlSymbols exists (left.tpe.baseType(_) != NoType) =>
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
              case dd@DefDef(_, UnapplyName, _, _, _, _) if isSynthetic(u)(dd) =>
                false
              case _ =>
                true
            } foreach { stat =>
              traverse(stat)
            }
          case Literal(Constant(null)) =>
            u.error(tree.pos, "null is disabled")
            super.traverse(tree)
          // Scala pattern matching outputs synthetic null.asInstanceOf[X]
          case ValDef(mods, _, _, _) if mods.hasFlag(Flag.MUTABLE) && synthetic =>
          // Ignore labels
          case LabelDef(_, _, _) if synthetic =>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
