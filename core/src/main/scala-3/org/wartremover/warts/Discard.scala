package org.wartremover
package warts

import scala.quoted.Type

object Discard extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      // 他にも検知したい型を列挙しよう！
      private def check[A <: AnyKind](t: Type[A]): Boolean = t match {
        case '[scala.concurrent.Future[?]] =>
          true
        case _ =>
          false
      }

      def msg(typeName: String): String = s"`${typeName}`の値を捨てている可能性があります"

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case t: Block =>
            t.statements.collect {
              case x: Term if check(x.tpe.asType) => x
            }.foreach { x =>
              error(x.pos, msg(x.tpe.show))
            }
            super.traverseTree(tree)(owner)
          case t: ClassDef =>
            t.body.collect {
              case x: Term if check(x.tpe.asType) => x
            }.foreach { x =>
              error(x.pos, msg(x.tpe.show))
            }
            super.traverseTree(tree)(owner)
          case f: DefDef if f.symbol.isAnonymousFunction =>
            val params = f.termParamss.flatMap(_.params).filter(x => check(x.tpt.tpe.asType))

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
                  error(x.pos, msg(x.tpt.tpe.show))
                }
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
