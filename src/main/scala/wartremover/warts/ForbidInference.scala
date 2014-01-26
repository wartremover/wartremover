package org.brianmckenna.wartremover
package warts

trait ForbidInference[T] extends WartTraverser {
  def applyForbidden(u: WartUniverse)(implicit t: u.TypeTag[T]): u.Traverser = {
    import u.universe._

    val CanEqualName: TermName = "canEqual"
    val EqualsName: TermName = "equals"
    val ProductElementName: TermName = "productElement"
    val ProductIteratorName: TermName = "productIterator"

    val tSymbol = typeOf[T].typeSymbol

    new Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
        def error() = u.error(tree.pos, s"Inferred type containing ${tSymbol.name}")

        tree match {
          case tpt @ TypeTree() if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) =>
            error()

          // Ignore case classes generated methods
          case ModuleDef(_, _, Template((_, _, statements))) if synthetic =>
            statements.foreach(super.traverse)
          case DefDef(_, CanEqualName | EqualsName | ProductElementName | ProductIteratorName, _, _, _, _) if synthetic =>

          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
