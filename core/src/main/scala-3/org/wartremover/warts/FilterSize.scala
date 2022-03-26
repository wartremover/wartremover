package org.wartremover
package warts

object FilterSize
    extends ExprMatch({
      case '{ ($x: collection.Iterable[t1]).filter($f).size } =>
        "you can use count instead of filter.size"
      case '{ ($x: collection.Seq[t1]).filter($f).length } =>
        "you can use count instead of filter.length"
    })
