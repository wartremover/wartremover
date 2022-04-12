package org.wartremover
package warts

object TryPartial
    extends ExprMatch({ case '{ ($x: scala.util.Try[t]).get } =>
      "Try#get is disabled"
    })
