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
  object AnyVal extends OrTypeLeastUpperBound[scala.AnyVal *: EmptyTuple]
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

      private var currentPosition: Option[Position] = None

      extension (self: Position) {
        def isNoPosition: Boolean = {
          self.sourceFile.getJPath.isEmpty || ((self.start == 0) && (self.end == 0))
        }
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case _: Typed =>
          case t: DefDef if (t.name == "valueOf") && t.symbol.flags.is(Flags.Synthetic) =>
          // ignore enum `valueOf` method
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
                  val left = t.left.dealias.show
                  val right = t.right.dealias.show
                  val pos = {
                    if (tree.pos.isNoPosition) {
                      currentPosition.getOrElse(tree.pos)
                    } else {
                      tree.pos
                    }
                  }
                  error(pos, s"least upper bound is `${lub.show}`. `${left} | ${right}`")
                }

                lub match {
                  case t: AppliedType =>
                    if (!tree.pos.isNoPosition) {
                      currentPosition = Some(tree.pos)
                    }
                    t.args.foreach(arg => traverseTree(TypeTree.of(using arg.asType))(owner))
                  case t: TypeRef =>
                    if (!tree.pos.isNoPosition) {
                      currentPosition = Some(tree.pos)
                    }
                    traverseTree(TypeTree.of(using t.qualifier.asType))(owner)
                  case _ =>
                }
              case t: AppliedType =>
                if (!tree.pos.isNoPosition) {
                  currentPosition = Some(tree.pos)
                }
                t.args.foreach(arg => traverseTree(TypeTree.of(using arg.asType))(owner))
              case t: TypeRef =>
                if (!tree.pos.isNoPosition) {
                  currentPosition = Some(tree.pos)
                }
                traverseTree(TypeTree.of(using t.qualifier.asType))(owner)
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
