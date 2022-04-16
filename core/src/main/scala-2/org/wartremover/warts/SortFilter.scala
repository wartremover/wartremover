package org.wartremover
package warts

object SortFilter extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    object IsSeq {
      private[this] val seq = rootMirror.staticClass("scala.collection.Seq").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< seq
    }
    object IsFunc1 {
      private[this] val f = rootMirror.staticClass("scala.Function1").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< f
    }
    object IsFunc2 {
      private[this] val f = rootMirror.staticClass("scala.Function2").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< f
    }
    object IsOrd {
      private[this] val o = rootMirror.staticClass("scala.math.Ordering").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< o
    }
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(
                Select(
                  Apply(
                    TypeApply(
                      Select(IsSeq(), TermName("sorted")),
                      _ :: Nil
                    ),
                    IsOrd() :: Nil
                  ),
                  TermName("filter")
                ),
                IsFunc1() :: Nil
              ) =>
            error(u)(tree.pos, "Change order of `sorted` and `filter`")
          case Apply(
                Select(
                  Apply(
                    Apply(
                      TypeApply(
                        Select(IsSeq(), TermName("sortBy")),
                        _ :: Nil
                      ),
                      IsFunc1() :: Nil
                    ),
                    IsOrd() :: Nil
                  ),
                  TermName("filter")
                ),
                IsFunc1() :: Nil
              ) =>
            error(u)(tree.pos, "Change order of `sortBy` and `filter`")
          case Apply(
                Select(
                  Apply(
                    Select(IsSeq(), TermName("sortWith")),
                    IsFunc2() :: Nil
                  ),
                  TermName("filter")
                ),
                IsFunc1() :: Nil
              ) =>
            error(u)(tree.pos, "Change order of `sortWith` and `filter`")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
