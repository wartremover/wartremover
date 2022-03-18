package org.wartremover
package warts

object JavaConversions extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser {
    }
  }
}
