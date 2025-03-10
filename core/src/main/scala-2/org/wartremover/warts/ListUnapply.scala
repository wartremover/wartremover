package org.wartremover
package warts

object ListUnapply extends WartTraverser {
  private[wartremover] def message: String = "Don't use `::` unapply method"

  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new Traverser {
      def isListConsUnapply(t: Tree): Boolean = {
        PartialFunction.cond(t) {
          case CaseDef(Apply(a: TypeTree, List(_, _)), _, _) =>
            a.original.tpe =:= typeOf[::.type]
          case CaseDef(Bind(_, Apply(a: TypeTree, List(_, _))), _, _) =>
            a.original.tpe =:= typeOf[::.type]
        }
      }

      private[this] val listType = rootMirror.staticClass("scala.collection.immutable.List").asType.toTypeConstructor

      override def traverse(tree: Tree): Unit = {

        tree match {
          case t if hasWartAnnotation(u)(t) =>
          // Ignore trees marked by SuppressWarnings
          case m: Match =>
            if (m.selector.tpe.dealias.typeConstructor <:< listType) {
              // this is a list
            } else {
              // report error for each `case`
              m.cases.withFilter(isListConsUnapply).foreach { badCase =>
                error(u)(badCase.pos, message)
              }
            }
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }

  }

}
