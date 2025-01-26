package org.wartremover
package warts

import scala.quoted.Type

object ListUnapplySeq extends WartTraverser {
  private[wartremover] def message: String = "Don't use List.unapplySeq"

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      val List(listUnapplySeq) = TypeRepr.of[scala.collection.immutable.List.type].typeSymbol.methodMember("unapplySeq")

      def isListUnapplySeq(t: Tree): Boolean =
        PartialFunction.cond(t) {
          case TypedOrTest(Unapply(f: TypeApply, _, _), _) =>
            f.fun.symbol == listUnapplySeq
          case Bind(_, t @ TypedOrTest(Unapply(f: TypeApply, _, _), _)) =>
            f.fun.symbol == listUnapplySeq
        }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case m: Match =>
            if (m.scrutinee.tpe.classSymbol == TypeRepr.of[List[?]].classSymbol) {
              // this is a list
            } else {
              m.cases.map(_.pattern).withFilter(isListUnapplySeq).foreach { x =>
                error(x.pos, message)
              }
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
