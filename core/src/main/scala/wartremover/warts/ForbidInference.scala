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
          case t if hasWartAnnotation(u)(t) =>      // Ignore trees marked by SuppressWarnings
          case TypeApply(fun, args) =>
            def evil(t: Type) = t contains tSymbol
            def altEvil(t: reflect.internal.Types#Type) = evil(t.asInstanceOf[Type])
            def paramtpes = fun.tpe.typeParams map (_.info.asInstanceOf[reflect.internal.Types#Type])
            def warnable = !(paramtpes.map(_.bounds.lo).exists(_.dealiasWidenChain exists altEvil))
            args foreach {
              case tpt @ TypeTree() if wasInferred(u)(tpt) && warnable && evil(tpt.tpe) =>
                tpt.tpe match {
                  case ExistentialType(_, _) =>     // Ignore existential types, they supposedly contain "any"
                  case _ => error()
                }
              case _ =>
            }
          case tpt @ TypeTree() if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) =>
            tpt.tpe match {
              case ExistentialType(_, _) =>         // Ignore existential types, they supposedly contain "any"
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
