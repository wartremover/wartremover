package org.wartremover
package warts

object SeqUpdated
    extends ExprMatch({ case '{ ($x: collection.Seq[t1]).updated($n, $y) } =>
      "Seq.updated is disabled"
    })
