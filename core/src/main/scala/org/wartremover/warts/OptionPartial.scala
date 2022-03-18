package org.wartremover
package warts

object OptionPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val optionSymbol = rootMirror.staticClass("scala.Option")
    val GetName = TermName("get")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(left, GetName) if left.tpe.baseType(optionSymbol) != NoType =>
            error(u)(tree.pos, "Option#get is disabled - use Option#fold instead")
          // TODO: This ignores a lot
          case LabelDef(_, _, rhs) if isSynthetic(u)(tree)=>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
