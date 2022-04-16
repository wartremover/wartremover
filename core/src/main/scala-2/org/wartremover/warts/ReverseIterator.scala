package org.wartremover
package warts

object ReverseIterator extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val seq = rootMirror.staticClass("scala.collection.Seq").toTypeConstructor
    val int = typeOf[Int]
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(Select(left, TermName("reverse")), TermName("iterator")) if left.tpe.typeConstructor <:< seq =>
            error(u)(tree.pos, "you can use reverseIterator instead of reverse.iterator")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
