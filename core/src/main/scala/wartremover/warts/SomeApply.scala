package org.wartremover
package warts

object SomeApply extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Apply(TypeApply(Select(Select(Ident(TermName("scala")), TermName("Some")), TermName("apply")), _), _) =>
            u.error(tree.pos, "Some.apply is disabled - use Option.apply instead")
          case v =>
            println(v)
            super.traverse(tree)
        }
      }
    }
  }
}
