package org.wartremover
package warts

object GetOrElseNull
    extends ExprMatch { case '{ ($x: Option[t]).getOrElse(null) } =>
      "you can use orNull instead of getOrElse(null)"
    }
