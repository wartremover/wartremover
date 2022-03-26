package org.wartremover
package warts

object ReverseFind
    extends ExprMatch({ case '{ ($x: collection.Seq[t]).reverse.find($f) } =>
      "you can use findLast instead of reverse.find"
    })
