package org.wartremover
package warts

object DropTakeToSlice extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val iterable = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor
    val int = typeOf[Int]
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(Select(Apply(Select(left, TermName("drop")), a1 :: Nil), TermName("take")), a2 :: Nil)
              if left.tpe.typeConstructor <:< iterable && a1.tpe <:< int && a2.tpe <:< int =>
            error(u)(tree.pos, "you can use slice instead of drop.take")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
