package org.brianmckenna.wartremover
package warts

object MutableDataStructures extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val mutablePackage = rootMirror.staticPackage("scala.collection.mutable")

    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Select(tpt, _) if tpt.tpe.contains(mutablePackage) && tpt.tpe.termSymbol.isPackage =>
            u.error(tree.pos, "scala.collection.mutable package is disabled -  use java.util instead")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
