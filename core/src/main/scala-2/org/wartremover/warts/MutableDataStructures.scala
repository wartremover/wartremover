package org.wartremover
package warts

object MutableDataStructures extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t @ TypeTree() if t.tpe.typeSymbol.fullName.startsWith("scala.collection.mutable.") =>
            error(u)(tree.pos, "scala.collection.mutable package is disabled")
            super.traverse(tree)
          case _ => super.traverse(tree)
        }
      }
    }
  }
}
