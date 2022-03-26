package org.wartremover
package warts

import scala.annotation.nowarn

@nowarn("msg=LazyList")
object RedundantConversions
    extends ExprMatch({
      case '{ ($x: List[t]).toList } =>
        "redundant toList conversion"
      case '{ ($x: collection.immutable.Seq[t]).toSeq } =>
        "redundant toSeq conversion"
      case '{ ($x: Vector[t]).toVector } =>
        "redundant toVector conversion"
      case '{ ($x: Stream[t]).toStream } =>
        "redundant toStream conversion"
      case '{
            type t1
            type t2 >: `t1`
            ($x: Set[`t1`]).toSet[`t2`]
          } =>
        // note https://github.com/lampepfl/dotty/issues/14708
        "redundant toSet conversion"
      case '{ ($x: collection.immutable.IndexedSeq[t]).toIndexedSeq } =>
        "redundant toIndexedSeq conversion"
      case '{ ($x: String).toString } =>
        "redundant toString conversion"
    })
