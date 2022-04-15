package org.wartremover
package warts

object FilterHeadOption extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val iterable = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor
    val function = rootMirror.staticClass("scala.Function1").toTypeConstructor
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(Apply(Select(left, TermName("filter")), f1 :: Nil), TermName("headOption"))
              if left.tpe.typeConstructor <:< iterable && f1.tpe.typeConstructor <:< function =>
            error(u)(tree.pos, "you can use find instead of filter.headOption")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
