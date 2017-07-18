package org.wartremover
package warts

object AnyVal extends ForbidInference[AnyVal] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
