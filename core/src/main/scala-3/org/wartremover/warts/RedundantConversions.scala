package org.wartremover
package warts

import scala.annotation.nowarn

object RedundantConversions extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      @nowarn("msg=LazyList")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "to") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: List[?]).toList } =>
                error(t.pos, "redundant toList conversion")
              case '{ ($x: collection.immutable.Seq[?]).toSeq } =>
                error(t.pos, "redundant toSeq conversion")
              case '{ ($x: Vector[?]).toVector } =>
                error(t.pos, "redundant toVector conversion")
              case '{ ($x: Stream[?]).toStream } =>
                error(t.pos, "redundant toStream conversion")
              case '{
                    type t1
                    type t2 >: `t1`
                    ($x: Set[`t1`]).toSet[`t2`]
                  } =>
                // note https://github.com/scala/scala3/issues/14708
                error(t.pos, "redundant toSet conversion")
              case '{ ($x: collection.immutable.IndexedSeq[?]).toIndexedSeq } =>
                error(t.pos, "redundant toIndexedSeq conversion")
              case '{ ($x: String).toString } =>
                error(t.pos, "redundant toString conversion")
              case '{ ($x: Int).toInt } =>
                error(t.pos, "redundant toInt conversion")
              case '{ ($x: Long).toLong } =>
                error(t.pos, "redundant toLong conversion")
              case '{ ($x: Float).toFloat } =>
                error(t.pos, "redundant toFloat conversion")
              case '{ ($x: Double).toDouble } =>
                error(t.pos, "redundant toDouble conversion")
              case '{ ($x: Byte).toByte } =>
                error(t.pos, "redundant toByte conversion")
              case '{ ($x: Short).toShort } =>
                error(t.pos, "redundant toShort conversion")
              case '{ ($x: Char).toChar } =>
                error(t.pos, "redundant toChar conversion")

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
