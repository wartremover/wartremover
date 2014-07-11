package org.brianmckenna.wartremover
package warts

object Nothing extends ForbidInference[Nothing] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
