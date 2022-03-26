package org.wartremover
package warts

object AsInstanceOf
    extends ExprMatch({ case '{ ($x: t1).asInstanceOf[t2] } =>
      "asInstanceOf is disabled"
    })
