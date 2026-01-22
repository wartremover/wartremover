package org.wartremover
package warts

object OptionPartial extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "get") =>
          case t if hasWartAnnotation(t) =>
          case Select(t, "get") if t.tpe.baseClasses.exists(_.fullName == "scala.Option") =>
            error(t.pos, "Option#get is disabled - use Option#fold instead")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
