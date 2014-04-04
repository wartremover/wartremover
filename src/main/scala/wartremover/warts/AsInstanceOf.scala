package org.brianmckenna.wartremover
package warts

object AsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val EqualsName: TermName = "equals"
    val AsInstanceOfName: TermName = "asInstanceOf"
    new u.Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
       tree match {

          // Ignore usage in synthetic classes
          case ClassDef(_, _, _, _) if synthetic => 

          // Ignore synthetic equals()
          case DefDef(_, EqualsName, _, _, _, _) if synthetic => 

          // Pattern matcher writes var x1 = null.asInstanceOf[...]
          case ValDef(mods, _, _, _) if mods.hasFlag(Flag.MUTABLE) && synthetic =>

          // Otherwise it's verboten for non-synthetic exprs
          case Select(e, AsInstanceOfName) if !isSynthetic(u)(e) =>
            u.error(tree.pos, "asInstanceOf is disabled")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}


