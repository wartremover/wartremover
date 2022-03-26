package org.wartremover
package warts

object FilterHeadOption
    extends ExprMatch({ case '{ ($x: collection.Iterable[t1]).filter($f).headOption } =>
      "you can use find instead of filter.headOption"
    })
