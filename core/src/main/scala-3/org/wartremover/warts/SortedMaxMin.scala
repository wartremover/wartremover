package org.wartremover
package warts

object SortedMaxMin
    extends ExprMatch {
      case '{
            type t1
            ($x: collection.Seq[`t1`]).sorted($o: Ordering[`t1`]).head
          } =>
        "You can use min instead of sorted.head"
      case '{
            type t1
            ($x: collection.Seq[`t1`]).sorted($o: Ordering[`t1`]).last
          } =>
        "You can use max instead of sorted.last"
      case '{
            type t1
            type t2
            ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])($o: Ordering[`t2`]).head
          } =>
        "You can use minBy instead of sortBy.head"
      case '{
            type t1
            type t2
            ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])($o: Ordering[`t2`]).last
          } =>
        "You can use maxBy instead of sortBy.last"
    }
