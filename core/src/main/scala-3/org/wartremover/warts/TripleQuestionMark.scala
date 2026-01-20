package org.wartremover
package warts

object TripleQuestionMark extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case i: Ident if "scala.Predef$.???" == i.symbol.fullName =>
            error(tree.pos, "??? is disabled")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
