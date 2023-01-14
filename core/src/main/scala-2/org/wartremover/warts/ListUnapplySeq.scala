package org.wartremover
package warts

object ListUnapplySeq extends WartTraverser {
  private[wartremover] def message: String = "Don't use List.unapplySeq"

  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new Traverser {
      def isListConsUnapply(t: Tree): Boolean = {
        PartialFunction.cond(t) {
          case CaseDef(UnApply(Apply(TypeApply(Select(list, TermName("unapplySeq")), _), _), _), _, _) =>
            list.tpe <:< typeOf[List.type]
          case CaseDef(Bind(_, UnApply(Apply(TypeApply(Select(list, TermName("unapplySeq")), _), _), _)), _, _) =>
            list.tpe <:< typeOf[List.type]
        }
      }

      private[this] val listType = rootMirror.staticClass("scala.collection.immutable.List").asType.toTypeConstructor

      override def traverse(tree: Tree): Unit = {

        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case m: Match =>
            if (m.selector.tpe.dealias.typeConstructor <:< listType) {
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
