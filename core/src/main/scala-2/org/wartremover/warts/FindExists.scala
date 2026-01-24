package org.wartremover
package warts

object FindExists extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val iterable = rootMirror.staticClass("scala.collection.Iterable").toTypeConstructor
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(
                Apply(Select(left, TermName("find")), _ :: Nil),
                TermName(name @ ("isDefined" | "nonEmpty" | "isEmpty"))
              ) if left.tpe.typeConstructor <:< iterable =>
            error(u)(tree.pos, s"you can use exists instead of find and ${name}")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
