package org.wartremover
package warts

object FindExists extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val iterableSymbol = Symbol.requiredClass("scala.collection.Iterable")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "find") =>
          case _ if hasWartAnnotation(tree) =>
          case Select(Apply(Select(x, "find"), _ :: Nil), method @ ("isEmpty" | "nonEmpty" | "isDefined"))
              if x.tpe.derivesFrom(iterableSymbol) =>
            error(tree.pos, s"you can use exists instead of find and ${method}")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
