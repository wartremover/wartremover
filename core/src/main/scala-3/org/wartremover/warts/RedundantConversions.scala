package org.wartremover
package warts

import scala.annotation.nowarn

@nowarn("msg=LazyList")
object RedundantConversions extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: List[t]).toList } =>
                error(tree.pos, "redundant toList conversion")
              case '{ ($x: collection.immutable.Seq[t]).toSeq } =>
                error(tree.pos, "redundant toSeq conversion")
              case '{ ($x: Vector[t]).toVector } =>
                error(tree.pos, "redundant toVector conversion")
              case '{ ($x: Stream[t]).toStream } =>
                error(tree.pos, "redundant toStream conversion")
              case '{
                    type t1
                    type t2 >: `t1`
                    ($x: Set[`t1`]).toSet[`t2`]
                  } =>
                // note https://github.com/lampepfl/dotty/issues/14708
                error(tree.pos, "redundant toSet conversion")
              case '{ ($x: collection.immutable.IndexedSeq[t]).toIndexedSeq } =>
                error(tree.pos, "redundant toIndexedSeq conversion")
              case '{ ($x: String).toString } =>
                error(tree.pos, "redundant toString conversion")
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
