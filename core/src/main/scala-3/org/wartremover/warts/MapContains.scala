package org.wartremover
package warts

object MapContains extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case Select(Apply(Select(m, "get"), Seq(arg)), "isDefined" | "isEmpty" | "nonEmpty")
              if m.tpe.derivesFrom(Symbol.requiredClass("scala.collection.Map")) =>
            error(tree.pos, "Maybe you can use `contains`")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
