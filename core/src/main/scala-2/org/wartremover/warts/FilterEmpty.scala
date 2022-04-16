package org.wartremover
package warts

object FilterEmpty extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    object IsFunc1 {
      private[this] val f = rootMirror.staticClass("scala.Function1").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< f
    }
    object IsIterable {
      private[this] val i = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< i
    }
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(
                Apply(
                  Select(IsIterable(), TermName("filter")),
                  IsFunc1() :: Nil
                ),
                TermName(a @ ("isEmpty" | "nonEmpty"))
              ) =>
            error(u)(tree.pos, s"you can use exists instead of filter.${a}")
          case Select(
                Apply(
                  Select(IsIterable(), TermName("filterNot")),
                  IsFunc1() :: Nil
                ),
                TermName(a @ ("isEmpty" | "nonEmpty"))
              ) =>
            error(u)(tree.pos, s"you can use forall instead of filterNot.${a}")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
