package org.wartremover
package warts

import scala.quoted.Expr
import scala.quoted.Type
import scala.quoted.Quotes
import scala.quoted.quotes

object MapUnit extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "map") =>
          case _ if hasWartAnnotation(tree) =>
          case _ if tree.isExpr =>
            tree.asExpr match {
              case '{
                    type t1
                    type t2
                    ($x: collection.Iterable[`t1`]).map($f: Function1[`t1`, `t2`])
                  } if TypeRepr.of[t2] =:= TypeRepr.of[Unit] =>
                error(tree.pos, "Maybe you should use `foreach` instead of `map`")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
