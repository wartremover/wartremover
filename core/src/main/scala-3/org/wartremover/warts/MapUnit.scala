package org.wartremover
package warts

object MapUnit extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val iterableSymbol = Symbol.requiredClass("scala.collection.Iterable")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "map") =>
          case _ if hasWartAnnotation(tree) =>
          case Apply(TypeApply(s @ Select(x, "map"), _), f :: Nil) if x.tpe.derivesFrom(iterableSymbol) =>
            f.tpe.asType match {
              case '[Function1[t1, t2]] if TypeRepr.of[t2] =:= TypeRepr.of[Unit] =>
                error(selectNamePosition(s), "Maybe you should use `foreach` instead of `map`")
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
