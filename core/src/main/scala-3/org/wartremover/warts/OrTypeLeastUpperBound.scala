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

      def andTypes(t: TypeRepr): List[TypeRepr] = {
        t match {
          case x: AndType =>
            andTypes(x.left) ::: andTypes(x.right)
          case _ =>
            t :: Nil
        }
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit =
        impl(tree, None, owner)

      private[this] def impl(tree: Tree, positionOpt: Option[Position], owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case a: Inferred =>
            a.tpe match {
              case t: OrType
                  if !t.left.typeSymbol.flags.is(Flags.Private) && !t.right.typeSymbol.flags.is(Flags.Private) =>
                val lub = {
                  implicit val ctx = q.asInstanceOf[QuotesImpl].ctx
                  t.asInstanceOf[DottyType].widenUnion.asInstanceOf[TypeRepr]
                }
                val lubAndTypes = andTypes(lub)
                if (types.exists(x => lubAndTypes.exists(x =:= _))) {
                  val left = t.left.show
                  val right = t.right.show
                  val pos = positionOpt.getOrElse(tree.pos)
                  error(pos, s"least upper bound is `${lub.show}`. `${left} | ${right}`")
                }
              case t: AppliedType =>
                t.args.foreach(arg => impl(TypeTree.of(using arg.asType), Some(positionOpt.getOrElse(a.pos)), owner))
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
