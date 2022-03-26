package org.wartremover
package warts

object CollectHeadOption
    extends ExprMatch({ case '{ ($x: collection.Iterable[t1]).collect($f).headOption } =>
      "you can use collectFirst instead of collect.headOption"
    })
