package org.wartremover
package warts

object ForeachEntry extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser =
    new u.Traverser {}
}
