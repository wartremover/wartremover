package org.brianmckenna.wartremover
package warts

object Var extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val HashCodeName: TermName = "hashCode"
    val metaDataSymbol = rootMirror.staticClass("scala.xml.MetaData")
    val namespaceBindingSymbol = rootMirror.staticClass("scala.xml.NamespaceBinding")

    new Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
        tree match {
          // Ignore case class's synthetic hashCode
          case ClassDef(mods, _, tparams, Template(parents, self, stats)) if mods.hasFlag(Flag.CASE) =>
            mods.annotations foreach { annotation =>
              traverse(annotation)
            }
            tparams foreach { tparam =>
              traverse(tparam)
            }
            parents foreach { parent =>
              traverse(parent)
            }
            traverse(self)
            stats filter {
              case DefDef(_, HashCodeName, _, _, _, _) =>
                false
              case _ =>
                true
            } foreach { stat =>
              traverse(stat)
            }
          // Scala pattern matching outputs synthetic vars
          case ValDef(mods, _, _, _) if mods.hasFlag(Flag.MUTABLE) && synthetic =>

          // XML literals output vars
          case ValDef(mods, _, tpt, _) if tpt.tpe.baseType(metaDataSymbol) != NoType || tpt.tpe.baseType(namespaceBindingSymbol) != NoType =>

          case ValDef(mods, _, tpt, _) if mods.hasFlag(Flag.MUTABLE) =>
            u.error(tree.pos, "var is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
