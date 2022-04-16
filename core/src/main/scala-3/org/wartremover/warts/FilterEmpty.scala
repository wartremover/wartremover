package org.wartremover
package warts

object FilterEmpty
    extends ExprMatch({
      case '{ ($x: collection.Iterable[t1]).filter($f).isEmpty } =>
        "you can use exists instead of filter.isEmpty"
      case '{ ($x: collection.Iterable[t1]).filter($f).nonEmpty } =>
        "you can use exists instead of filter.nonEmpty"
      case '{ ($x: collection.Iterable[t1]).filterNot($f).isEmpty } =>
        "you can use forall instead of filterNot.isEmpty"
      case '{ ($x: collection.Iterable[t1]).filterNot($f).nonEmpty } =>
        "you can use forall instead of filterNot.nonEmpty"
    })
