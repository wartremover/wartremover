package org.wartremover
package warts

object MapUnit extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val iterable = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Apply(
                Apply(
                  TypeApply(Select(qualifier, TermName("map")), _ :: _ :: Nil),
                  f :: Nil
                ),
                canBuildFrom :: Nil
              ) if (qualifier.tpe.typeConstructor <:< iterable) && f.tpe.typeArgs.lift(1).exists(_ =:= typeOf[Unit]) =>
            error(u)(tree.pos, "Maybe you should use `foreach` instead of `map`")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
