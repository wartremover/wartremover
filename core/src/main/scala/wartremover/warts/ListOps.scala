package org.brianmckenna.wartremover
package warts

object ListOps extends WartTraverser {

  class Op(name: String, error: String) extends WartTraverser {
    def apply(u: WartUniverse): u.Traverser = {
      import u.universe._

      val listSymbol = rootMirror.staticClass("scala.collection.immutable.List")
      val Name: TermName = name
      new u.Traverser {
        override def traverse(tree: Tree): Unit = {
          tree match {
            case Select(left, Name) if left.tpe.baseType(listSymbol) != NoType ⇒
              u.error(tree.pos, error)
            // TODO: This ignores a lot
            case LabelDef(_, _, rhs) if isSynthetic(u)(tree) ⇒
            case _ ⇒
              super.traverse(tree)
          }
        }
      }
    }
  }

  def apply(u: WartUniverse): u.Traverser =
    WartTraverser.sumList(u)(List(
      new Op("head", "List#head is disabled - use List#headOption instead"),
      new Op("tail", "List#tail is disabled - use List#drop(1) instead"),
      new Op("last", "List#last is disabled - use List#lastOption instead")
    ))

}
