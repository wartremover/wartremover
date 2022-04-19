package org.wartremover
package warts

object SortedMaxMin extends WartTraverser {

  private[this] val methods: Map[String, String] = Map(
    "head" -> "min",
    "last" -> "max",
  )

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
    object IsOrd {
      private[this] val o = rootMirror.staticClass("scala.math.Ordering").toTypeConstructor
      def unapply(t: Tree): Boolean = t.tpe.typeConstructor <:< o
    }
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(
                Apply(
                  TypeApply(
                    Select(IsSeq(), TermName("sorted")),
                    _ :: Nil
                  ),
                  IsOrd() :: Nil
                ),
                TermName(method @ ("head" | "last"))
              ) =>
            error(u)(tree.pos, s"You can use ${methods(method)} instead of sorted.${method}")
          case Select(
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
                TermName(method @ ("head" | "last"))
              ) =>
            error(u)(tree.pos, s"You can use ${methods(method)}By instead of sortBy.${method}")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
