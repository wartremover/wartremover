package org.wartremover
package warts

object Serializable extends ForbidInference[Serializable] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
