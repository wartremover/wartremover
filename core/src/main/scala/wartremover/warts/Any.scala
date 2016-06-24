package org.wartremover
package warts

object Any extends ForbidInference[Any] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
