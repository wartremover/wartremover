package org.wartremover
package warts

import scala.quoted.Type
import scala.quoted.Quotes

object Discard {
  object Either
      extends Discard([a <: AnyKind] =>
        (tpe: Type[a]) =>
          tpe match {
            case '[Either[?, ?]] =>
              true
            case _ =>
              false
          }
      )

  object Future
      extends Discard([a <: AnyKind] =>
        (tpe: Type[a]) =>
          tpe match {
            case '[scala.concurrent.Future[?]] =>
              true
            case _ =>
              false
          }
      )

  object Try
      extends Discard([a <: AnyKind] =>
        (tpe: Type[a]) =>
          tpe match {
            case '[scala.util.Try[?]] =>
              true
            case _ =>
              false
          }
      )
}

abstract class Discard(filter: Quotes ?=> ([a <: AnyKind] => Type[a] => Boolean)) extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      def msg[B](t: TypeRepr): String =
        s"discard `${t.dealias.show}`"

      def filterFunc(tpe: TypeRepr): Boolean = {
        !(tpe =:= TypeRepr.of[Nothing]) && filter(tpe.asType)
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case t: Block =>
            t.statements.collect {
              case x: Term if filterFunc(x.tpe) => x
            }.foreach { x =>
              error(x.pos, msg(x.tpe))
            }
            super.traverseTree(tree)(owner)
          case t: ClassDef =>
            t.body.collect {
              case x: Term if filterFunc(x.tpe) => x
            }.foreach { x =>
              error(x.pos, msg(x.tpe))
            }
            super.traverseTree(tree)(owner)
          case f: DefDef if f.symbol.isAnonymousFunction =>
            val params = f.termParamss.flatMap(_.params).filter(x => filterFunc(x.tpt.tpe))

            if (params.nonEmpty) {
              f.rhs.foreach { body =>
                val accumulator = new TreeAccumulator[Set[String]] {
                  override def foldTree(x: Set[String], t: Tree)(owner: Symbol) = {
                    foldOverTree(
                      t match {
                        case i: Ident =>
                          x + i.name
                        case _ =>
                          x
                      },
                      t
                    )(owner)
                  }
                }
                val bodyNames: Set[String] = accumulator.foldTree(Set.empty, body)(owner)
                params.filterNot(p => bodyNames(p.name)).foreach { x =>
                  error(x.pos, msg(x.tpt.tpe))
                }
              }
            }
            super.traverseTree(tree)(owner)
          case f: CaseDef =>
            PartialFunction
              .condOpt(f.pattern) {
                case Bind(x, w: Wildcard) if filterFunc(w.tpe) =>
                  x -> w.tpe
                case x: Ident if filterFunc(x.tpe) =>
                  x.name -> x.tpe
              }
              .foreach { (name, tpe) =>
                val accumulator = new TreeAccumulator[Set[String]] {
                  override def foldTree(x: Set[String], t: Tree)(owner: Symbol) = {
                    foldOverTree(
                      t match {
                        case i: Ident =>
                          x + i.name
                        case _ =>
                          x
                      },
                      t
                    )(owner)
                  }
                }

                val namesSet: Set[String] =
                  accumulator.foldTree(Set.empty, f.rhs)(owner) ++ f.guard
                    .map(x => accumulator.foldTree(Set.empty, x)(owner))
                    .toSeq
                    .flatten

                if (namesSet(name)) {
                  // ok
                } else {
                  error(f.pattern.pos, msg(tpe))
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
