package org.wartremover
package warts

object SizeIs extends WartTraverser {
  private def sizeMessage = "Maybe you can use `sizeIs` instead of `size`"
  private def lengthMessage = "Maybe you can use `lengthIs` instead of `length`"

  private val methodNames: Seq[String] = Seq(
    "size",
    "length",
  )

  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val iterableSymbol = Symbol.requiredClass("scala.collection.Iterable")
      private val seqSymbol = Symbol.requiredClass("scala.collection.Seq")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if methodNames.forall(sourceCodeNotContains(tree, _)) =>
          case t if hasWartAnnotation(t) =>
          case Apply(Select(s @ Select(x1, "size"), "<" | "==" | "!=" | "<=" | ">" | ">="), x2 :: Nil)
              if x1.tpe.derivesFrom(iterableSymbol) && (x2.tpe <:< TypeRepr.of[Int]) =>
            error(selectNamePosition(s), sizeMessage)
          case Apply(Select(s @ Select(x1, "length"), "<" | "==" | "!=" | "<=" | ">" | ">="), x2 :: Nil)
              if x1.tpe.derivesFrom(seqSymbol) && (x2.tpe <:< TypeRepr.of[Int]) =>
            error(selectNamePosition(s), lengthMessage)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
