package org.brianmckenna.wartremover
package warts

object OptionPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val optionSymbol = rootMirror.staticClass("scala.Option")
    val GetName: TermName = "get"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Select(Ident(_), GetName) =>
            // Ignore synthetic Option#get generated from regex pattern match
          case Select(left, GetName) if left.tpe.baseType(optionSymbol) != NoType =>
            u.error(tree.pos, "Option#get is disabled - use Option#fold instead")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
