package org.wartremover
package warts

object Option2Iterable extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case Apply(method, _ :: Nil) if method.symbol.fullName == "scala.Option$.option2Iterable" =>
            error(tree.pos, "Implicit conversion from Option to Iterable is disabled - use Option#toList instead")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
