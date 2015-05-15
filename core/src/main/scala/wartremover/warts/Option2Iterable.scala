package org.brianmckenna.wartremover
package warts

object Option2Iterable extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(Select(This(TypeName("scala")), TermName("Option")), TermName("option2Iterable")) =>
            u.error(tree.pos, "Implicit conversion from Option to Iterable is disabled - use Option#toIterable instead")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
// Select(Select(This(TypeName("scala")), scala.Option), TermName("option2Iterable"))
