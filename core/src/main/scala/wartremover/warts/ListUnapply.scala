package org.wartremover
package warts

object ListUnapply extends WartTraverser {
  private[wartremover] def message: String = "Don't use `::` unapply method"

  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new Traverser {
      def isListConsUnapply(t: Tree): Boolean = {
        t match {
          case CaseDef(Apply(a: TypeTree, List(_, _)), _, _) if a.original.tpe =:= typeOf[::.type] =>
            true
          case _ =>
            false
        }
      }

      private[this] val listType = rootMirror.staticClass("scala.collection.immutable.List").asType.toTypeConstructor

      override def traverse(tree: Tree): Unit = {

        tree match {
          case t if hasWartAnnotation(u)(t) =>
            // Ignore trees marked by SuppressWarnings
          case m: Match =>
            if (m.selector.tpe.typeConstructor <:< listType) {
              // this is a list
            } else {
              // report error for each `case`
              m.cases.withFilter(isListConsUnapply).foreach { badCase =>
                error(u)(badCase.pos, message)
              }
            }
          case _ =>
            super.traverse(tree)
        }
      }
    }

  }

}
