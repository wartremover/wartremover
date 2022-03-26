package org.wartremover
package warts

object SortFilter
    extends ExprMatch({
      case '{
            type t1
            ($x: collection.Seq[`t1`]).sorted($o: Ordering[`t1`]).filter($p)
          } =>
        "Change order of `sorted` and `filter`"
      case '{
            type t1
            type t2
            ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])($o: Ordering[`t2`]).filter($p)
          } =>
        "Change order of `sortBy` and `filter`"
      case '{
            type t1
            ($x: collection.Seq[`t1`]).sortWith($f: Function2[`t1`, `t1`, Boolean]).filter($p)
          } =>
        "Change order of `sortWith` and `filter`"
    })
