package org.brianmckenna.wartremover
package warts

object Any extends ForbidInference[Any] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
