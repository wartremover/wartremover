package org.wartremover
package warts

object OptionPartial
    extends ExprMatch({ case '{ ($x: Option[t]).get } =>
      "Option#get is disabled - use Option#fold instead"
    })
