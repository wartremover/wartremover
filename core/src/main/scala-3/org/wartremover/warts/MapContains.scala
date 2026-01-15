package org.wartremover
package warts

object MapContains extends WartTraverser {
  private val checkMethodNames = Seq("isDefined", "isEmpty", "nonEmpty")

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "get") || checkMethodNames.forall(sourceCodeNotContains(tree, _)) =>
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
