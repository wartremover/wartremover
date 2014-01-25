package org.brianmckenna.wartremover
package warts

trait ForbidInference[T] extends WartTraverser {
  def applyForbidden(u: WartUniverse)(implicit t: u.TypeTag[T]): u.Traverser = {
    import u.universe._

    val tSymbol = typeOf[T].typeSymbol
    new Traverser {
      override def traverse(tree: Tree) {
        def error() = u.error(tree.pos, s"Inferred type containing ${tSymbol.name} from assignment")
        tree match {
          case ValDef(_, _, tpt: TypeTree, _) if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) =>
            error()
          case DefDef(_, _, _, _, tpt: TypeTree, _) if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) =>
            error()
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
