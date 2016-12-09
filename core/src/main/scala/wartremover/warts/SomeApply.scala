package org.wartremover
package warts

object SomeApply extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val scala: TermName = "scala"
    val some: TermName = "Some"
    val app: TermName = "apply"

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Apply(TypeApply(Select(Select(Ident(pkg), obj), method), _), _)
            if pkg == scala && obj == some && method == app =>
            u.error(tree.pos, "Some.apply is disabled - use Option.apply instead")
          case v =>
            super.traverse(tree)
        }
      }
    }
  }
}
