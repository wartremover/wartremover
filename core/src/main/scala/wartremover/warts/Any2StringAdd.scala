package org.wartremover
package warts

@deprecated("Use StringPlusAny", "Wartremover 1.1.2")
object Any2StringAdd extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val PredefName: TermName = "Predef"
    val Any2StringAddName: TermName = "any2stringadd"
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Apply(Select(Select(_, PredefName), Any2StringAddName), _) =>
            error(u)(tree.pos, "Scala inserted an any2stringadd call")
            super.traverse(tree)
          case TypeApply(Select(Select(_, PredefName), Any2StringAddName), _) =>
            error(u)(tree.pos, "Scala inserted an any2stringadd call")
            super.traverse(tree)
          case _ => super.traverse(tree)
        }
      }
    }
  }
}
