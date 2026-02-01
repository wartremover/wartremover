package org.wartremover
package warts

import scala.quoted.Type

object ListUnapply extends WartTraverser {
  private[wartremover] def message: String = "Don't use `::` unapply method"

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      val List(consUnapply) = TypeRepr.of[scala.collection.immutable.::.type].typeSymbol.methodMember("unapply")

      def isListConsUnapply(t: Tree): Boolean = {
        PartialFunction.cond(t) {
          case t @ TypedOrTest(Unapply(f: TypeApply, _, _), _) =>
            f.fun.symbol == consUnapply
          case Bind(_, TypedOrTest(Unapply(f: TypeApply, _, _), _)) =>
            f.fun.symbol == consUnapply
        }
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case m: Match =>
            if (m.scrutinee.tpe.classSymbol == TypeRepr.of[List[?]].classSymbol) {
              // this is a list
            } else {
              m.cases.map(_.pattern).withFilter(isListConsUnapply).foreach { x =>
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
