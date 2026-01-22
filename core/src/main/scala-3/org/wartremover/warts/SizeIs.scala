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
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if methodNames.forall(sourceCodeNotContains(tree, _)) =>
          case t if hasWartAnnotation(t) =>
          case Apply(Select(Select(x1, "size"), "<" | "==" | "!=" | "<=" | ">" | ">="), x2 :: Nil)
              if x1.tpe.baseClasses.exists(_.fullName == "scala.collection.Iterable") && (x2.tpe <:< TypeRepr
                .of[Int]) =>
            error(tree.pos, sizeMessage)
          case Apply(Select(Select(x1, "length"), "<" | "==" | "!=" | "<=" | ">" | ">="), x2 :: Nil)
              if x1.tpe.baseClasses.exists(_.fullName == "scala.collection.Seq") && (x2.tpe <:< TypeRepr.of[Int]) =>
            error(tree.pos, lengthMessage)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
