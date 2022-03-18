package org.wartremover
package warts

object SizeIs extends WartTraverser {
  private def sizeMessage = "Maybe you can use `sizeIs` instead of `size`"
  private def lengthMessage = "Maybe you can use `lengthIs` instead of `length`"

  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x1: Iterable[t]).size < ($x2: Int) } =>
                error(tree.pos, sizeMessage)
              case '{ ($x1: Iterable[t]).size == ($x2: Int) } =>
                error(tree.pos, sizeMessage)
              case '{ ($x1: Iterable[t]).size <= ($x2: Int) } =>
                error(tree.pos, sizeMessage)
              case '{ ($x1: Iterable[t]).size > ($x2: Int) } =>
                error(tree.pos, sizeMessage)
              case '{ ($x1: Iterable[t]).size >= ($x2: Int) } =>
                error(tree.pos, sizeMessage)

              case '{ ($x1: collection.Seq[t]).length < ($x2: Int) } =>
                error(tree.pos, lengthMessage)
              case '{ ($x1: collection.Seq[t]).length == ($x2: Int) } =>
                error(tree.pos, lengthMessage)
              case '{ ($x1: collection.Seq[t]).length == ($x2: Int) } =>
                error(tree.pos, lengthMessage)
              case '{ ($x1: collection.Seq[t]).length <= ($x2: Int) } =>
                error(tree.pos, lengthMessage)
              case '{ ($x1: collection.Seq[t]).length > ($x2: Int) } =>
                error(tree.pos, lengthMessage)
              case '{ ($x1: collection.Seq[t]).length >= ($x2: Int) } =>
                error(tree.pos, lengthMessage)

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
