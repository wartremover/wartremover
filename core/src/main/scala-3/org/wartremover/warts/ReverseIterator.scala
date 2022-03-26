package org.wartremover
package warts

object ReverseIterator
    extends ExprMatch({ case '{ ($x: collection.Seq[t]).reverse.iterator } =>
      "you can use reverseIterator instead of reverse.iterator"
    })
