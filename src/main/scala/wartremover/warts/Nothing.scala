package org.brianmckenna.wartremover
package warts

object Nothing extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val nothingSymbol = typeOf[Nothing].typeSymbol
    new Traverser {
      override def traverse(tree: Tree) {
        def error() = u.error(tree.pos, "Inferred type containing Nothing from assignment")
        tree match {
          case ValDef(_, _, tpt: TypeTree, _) if wasInferred(u)(tpt) && tpt.tpe.contains(nothingSymbol) =>
            error()
          case DefDef(_, _, _, _, tpt: TypeTree, _) if wasInferred(u)(tpt) && tpt.tpe.contains(nothingSymbol) =>
            error()
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
