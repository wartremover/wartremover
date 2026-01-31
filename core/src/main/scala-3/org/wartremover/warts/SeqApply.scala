package org.wartremover
package warts

object SeqApply extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val seqSymbol = Symbol.requiredClass("scala.collection.Seq")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case Apply(Select(obj, "apply"), _ :: Nil) if obj.tpe.derivesFrom(seqSymbol) =>
            error(tree.pos, "Seq.apply is disabled")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
