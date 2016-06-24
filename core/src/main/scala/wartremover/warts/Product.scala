package org.wartremover
package warts

object Product extends ForbidInference[Product] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
