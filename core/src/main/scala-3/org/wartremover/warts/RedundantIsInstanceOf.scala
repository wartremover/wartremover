package org.wartremover
package warts

object RedundantIsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "isInstanceOf") =>
          case _ if hasWartAnnotation(tree) =>
          case TypeApply(s @ Select(x1, "isInstanceOf"), x2 :: Nil) if x1.tpe <:< x2.tpe =>
            error(selectNamePosition(s), "redundant isInstanceOf")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
