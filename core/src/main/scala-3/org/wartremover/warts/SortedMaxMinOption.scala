package org.wartremover
package warts

object SortedMaxMinOption
    extends ExprMatch({
      case '{
            type t1
            ($x: collection.Seq[`t1`]).sorted($o: Ordering[`t1`]).headOption
          } =>
        "You can use minOption instead of sorted.headOption"
      case '{
            type t1
            ($x: collection.Seq[`t1`]).sorted($o: Ordering[`t1`]).lastOption
          } =>
        "You can use maxOption instead of sorted.lastOption"
      case '{
            type t1
            type t2
            ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])($o: Ordering[`t2`]).headOption
          } =>
        "You can use minByOption instead of sortBy.headOption"
      case '{
            type t1
            type t2
            ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])($o: Ordering[`t2`]).lastOption
          } =>
        "You can use maxByOption instead of sortBy.lastOption"
    })
