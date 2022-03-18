package org.wartremover
package warts

object NonUnitStatements extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        // TODO
      }
    }
  }
}
