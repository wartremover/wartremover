package org.wartremover
package warts

object Enumeration
    extends ForbidType[scala.Enumeration](
      "Enumeration is disabled - use case objects instead"
    )
