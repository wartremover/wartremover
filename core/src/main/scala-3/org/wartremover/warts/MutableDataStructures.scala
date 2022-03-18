package org.wartremover
package warts

object MutableDataStructures extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: TypeTree if t.tpe.classSymbol.exists(_.fullName.startsWith("scala.collection.mutable.")) =>
            error(tree.pos, "scala.collection.mutable package is disabled")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
