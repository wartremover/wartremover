package org.brianmckenna.wartremover
package warts

object TryPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val optionSymbol = rootMirror.staticClass("scala.util.Try")
    val GetName: TermName = "get"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Select(left, GetName) if left.tpe.baseType(optionSymbol) != NoType =>
            u.error(tree.pos, "Try#get is disabled")
          case LabelDef(_, _, rhs) if isSynthetic(u)(tree)=>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
