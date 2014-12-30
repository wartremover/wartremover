package org.brianmckenna.wartremover
package warts

object IsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val IsInstanceOfName: TermName = TermName("isInstanceOf")
    val CanEqualName: TermName = TermName("canEqual")
    val EqualsName: TermName = TermName("equals")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        val synthetic = isSynthetic(u)(tree)
        tree match {

          // Ignore synthetic canEquals() and equals()
          case DefDef(_, CanEqualName | EqualsName, _, _, _, _) if synthetic => 

          // Otherwise nope, for non-synthetic receivers
          case Select(id, IsInstanceOfName) if !isSynthetic(u)(id) =>
            u.error(tree.pos, "isInstanceOf is disabled")

          case _ => super.traverse(tree)

        }          
      }
    }
  }
}
