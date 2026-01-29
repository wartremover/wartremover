package org.wartremover
package warts

object SizeToLength extends WartTraverser {
  private[this] def message = "Maybe you should use `length` instead of `size`"
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val predefSymbol = rootMirror.staticModule("scala.Predef")
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Select(Apply(predefAugmentString, _ :: Nil), TermName("size"))
              if predefAugmentString.symbol.fullName == "scala.Predef.augmentString" =>
            error(u)(tree.pos, message)
            super.traverse(tree)
          case Select(Apply(Select(predef, TermName(arrayOps)), array :: Nil), TermName("size"))
              if array.tpe.typeSymbol.fullName == "scala.Array" && predef.symbol == predefSymbol && arrayOps.endsWith(
                "ArrayOps"
              ) && !isSynthetic(u)(tree) =>
            error(u)(tree.pos, message)
            super.traverse(tree)
          case Select(
                Apply(TypeApply(Select(predef, TermName("refArrayOps" | "genericArrayOps")), _ :: Nil), array :: Nil),
                TermName("size")
              )
              if array.tpe.typeSymbol.fullName == "scala.Array" && predef.symbol == predefSymbol && !isSynthetic(u)(
                tree
              ) =>
            error(u)(tree.pos, message)
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
