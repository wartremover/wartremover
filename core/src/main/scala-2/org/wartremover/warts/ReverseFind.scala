package org.wartremover
package warts

object ReverseFind extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val iterable = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor
    val function = rootMirror.staticClass("scala.Function1").toTypeConstructor
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(Select(Select(left, TermName("reverse")), TermName("find")), f1 :: Nil)
              if left.tpe.typeConstructor <:< iterable && f1.tpe.typeConstructor <:< function =>
            error(u)(tree.pos, "you can use findLast instead of reverse.find")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
