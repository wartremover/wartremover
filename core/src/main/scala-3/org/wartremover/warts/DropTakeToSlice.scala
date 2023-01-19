package org.wartremover
package warts

object DropTakeToSlice
    extends ExprMatch { case '{ ($x: collection.Iterable[t1]).drop($n).take($m) } =>
      "you can use slice instead of drop.take"
    }
