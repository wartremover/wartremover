package org.wartremover
package warts

object JavaSerializable extends ForbidInference[java.io.Serializable] {
  def apply(u: WartUniverse): u.Traverser = applyForbidden(u)
}
