package org.wartremover
package warts

object CollectHeadOption extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val iterable = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor
    new u.Traverser {
      private[this] def message = "you can use collectFirst instead of collect.headOption"
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(
                Apply(Apply(TypeApply(Select(left, TermName("collect")), _ :: _ :: Nil), pf :: Nil), canBuildFrom),
                TermName("headOption")
              ) if left.tpe.typeConstructor <:< iterable =>
            // Scala 2.12
            error(u)(tree.pos, message)
          case Select(Apply(TypeApply(Select(left, TermName("collect")), _ :: Nil), pf :: Nil), TermName("headOption"))
              if left.tpe.typeConstructor <:< iterable =>
            // Scala 2.13
            error(u)(tree.pos, message)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
