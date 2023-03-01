package org.wartremover
package warts

import scala.quoted.Quotes
import scala.quoted.Type
import scala.quoted.runtime.impl.QuotesImpl
import dotty.tools.dotc.core.Types.Type as DottyType

object OrTypeLeastUpperBound {
  object All
      extends OrTypeLeastUpperBound[
        (
          scala.Any,
          scala.AnyRef,
          scala.Matchable,
          scala.Product,
          scala.Serializable,
        )
      ]
  object Any extends OrTypeLeastUpperBound[scala.Any *: EmptyTuple]
  object AnyRef extends OrTypeLeastUpperBound[scala.AnyRef *: EmptyTuple]
  object Matchable extends OrTypeLeastUpperBound[scala.Matchable *: EmptyTuple]
  object Product extends OrTypeLeastUpperBound[scala.Product *: EmptyTuple]
  object Serializable extends OrTypeLeastUpperBound[scala.Serializable *: EmptyTuple]
}

abstract class OrTypeLeastUpperBound[A <: NonEmptyTuple](using getType: Quotes ?=> Type[A]) extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      def getTypes[T <: Tuple](using Type[T]): List[TypeRepr] = {
        @annotation.tailrec
        def loop[B <: Tuple](acc: List[TypeRepr])(using Type[B]): List[TypeRepr] = {
          Type.of[B] match {
            case '[x *: xs] =>
              loop[xs](TypeRepr.of[x] :: acc)
            case '[EmptyTuple] =>
              acc
          }
        }
        loop[T](Nil)
      }

      val types = getTypes[A]

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case a: ValDef if a.symbol.flags.is(Flags.Synthetic) =>
          // skip due to false positive
          // https://github.com/wartremover/wartremover/issues/787
          // TODO more better way
          case a: DefDef if a.symbol.flags.is(Flags.Synthetic) =>
          case a: Inferred =>
            a.tpe match {
              case t: OrType =>
                val lub = {
                  implicit val ctx = q.asInstanceOf[QuotesImpl].ctx
                  t.asInstanceOf[DottyType].widenUnion.asInstanceOf[TypeRepr]
                }
                if (types.exists(lub <:< _)) {
                  val left = t.left.show
                  val right = t.right.show
                  error(tree.pos, s"least upper bound is `${lub.show}`. `${left} | ${right}`")
                }
              case _ =>
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
