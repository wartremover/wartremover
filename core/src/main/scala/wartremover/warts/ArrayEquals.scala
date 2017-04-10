package org.wartremover
package warts

import reflect.NameTransformer

object ArrayEquals extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Equals: TermName = NameTransformer.encode("==")

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Apply(Select(array, Equals), _)
            if (array.tpe <:< typeOf[Array[_]] || array.tpe <:< typeOf[Iterator[_]]) && !isSynthetic(u)(tree) =>
            error(u)(tree.pos, "== is disabled, use sameElements instead")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
