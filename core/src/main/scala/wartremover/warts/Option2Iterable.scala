package org.wartremover
package warts

object Option2Iterable extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val scala = newTypeName("scala")
    val option = newTermName("Option")
    val option2Iterable = newTermName("option2Iterable")

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(Select(This(pkg), obj), method)
            if pkg == scala && obj == option && method == option2Iterable =>
            u.error(tree.pos, "Implicit conversion from Option to Iterable is disabled - use Option#toList instead")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
