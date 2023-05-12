package org.wartremover
package warts

object SeqApply extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val symbol = rootMirror.staticClass("scala.collection.Seq")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(left, TermName("apply")) if left.tpe.baseType(symbol) != NoType =>
            error(u)(tree.pos, "Seq.apply is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
