package org.wartremover
package warts

object SeqApply
    extends ExprMatch({ case '{ ($x: collection.Seq[t1]).apply($n) } =>
      "Seq.apply is disabled"
    })
