package org.brianmckenna.wartremover
package warts

object AsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val EqualsName: TermName = "equals"
    val AsInstanceOfName: TermName = "asInstanceOf"
    new Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
        tree match {

          // Ignore usage in synthetic classes
          case ClassDef(_, _, _, _) if synthetic => 

          // Ignore synthetic equals()
          case DefDef(_, EqualsName, _, _, _, _) if synthetic => 

          // Otherwise it's verboten
          case u.universe.Select(_, AsInstanceOfName) =>
            u.error(tree.pos, "asInstanceOf is disabled")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}


