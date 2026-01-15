package org.wartremover
package warts

object ThreadSleep extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "sleep") =>
          case t if hasWartAnnotation(t) =>
          case Apply(Select(threadObject, "sleep"), _) if threadObject.symbol.fullName == "java.lang.Thread" =>
            error(tree.pos, "don't use Thread.sleep")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
