package org.wartremover
package warts

import reflect.NameTransformer

object Equals extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val Equals: TermName = NameTransformer.encode("==")

    new Traverser {
      override def traverse(tree: Tree) = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case _ if isSynthetic(u)(tree) =>

          case Apply(Select(lhs, Equals), _) =>
            u.error(tree.pos, s"== is disabled - use === or equivalent instead")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}
