package org.wartremover
package warts

object ListAppend extends WartTraverser {
  private[wartremover] def message: String = "Don't use List `:+` method because too slow"
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($list: List[tpe1]) :+ ($value: tpe2) } =>
                error(t.pos, message)
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
