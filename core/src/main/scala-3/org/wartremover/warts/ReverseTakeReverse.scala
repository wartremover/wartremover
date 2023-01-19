package org.wartremover
package warts

object ReverseTakeReverse
    extends ExprMatch { case '{ ($x: collection.Seq[t1]).reverse.take($n).reverse } =>
      "you can use takeRight instead of reverse.take.reverse"
    }
