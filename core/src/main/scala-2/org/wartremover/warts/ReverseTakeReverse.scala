package org.wartremover
package warts

object ReverseTakeReverse extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val seq = rootMirror.staticClass("scala.collection.Seq").toTypeConstructor
    val int = typeOf[Int]
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(
                Apply(Select(Select(left, TermName("reverse")), TermName("take")), a1 :: Nil),
                TermName("reverse")
              ) if left.tpe.typeConstructor <:< seq && a1.tpe <:< int =>
            error(u)(tree.pos, "you can use takeRight instead of reverse.take.reverse")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
