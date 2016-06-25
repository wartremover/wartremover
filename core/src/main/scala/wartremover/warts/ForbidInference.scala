package org.wartremover
package warts

trait ForbidInference[T] extends WartTraverser {
  def applyForbidden(u: WartUniverse)(implicit t: u.TypeTag[T]): u.Traverser = {
    import u.universe._

    val CanEqualName: TermName = "canEqual"
    val EqualsName: TermName = "equals"
    val ProductElementName: TermName = "productElement"
    val ProductIteratorName: TermName = "productIterator"

    val tSymbol = typeOf[T].typeSymbol

    // Scala compiler inserts stuff like "extends AnyRef with Serializable"
    // This method filters those out.
    def explicitParents(parents: List[Tree]): List[Tree] = parents.collect {
      case tpt @ TypeTree() if !wasInferred(u)(tpt) =>
        tpt
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        val synthetic = isSynthetic(u)(tree)
        def error() = u.error(tree.pos, s"Inferred type containing ${tSymbol.name}")

        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case tpt @ TypeTree() if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) =>
            tpt.tpe match {
              // Ignore existential types, they supposedly contain "any"
              case ExistentialType(_, _) =>

              case _ =>
                error()
            }

          // Ignore case classes generated methods
          case ModuleDef(_, _, Template((parents, self, statements))) =>
            explicitParents(parents).foreach(traverse)
            traverse(self)
            statements.foreach(traverse)
          case ClassDef(_, _, _, Template((parents, self, statements))) if synthetic =>
            explicitParents(parents).foreach(traverse)
            traverse(self)
            statements.foreach(traverse)
          case DefDef(_, CanEqualName | EqualsName | ProductElementName | ProductIteratorName, _, _, _, _) if synthetic =>

          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
