package org.brianmckenna.wartremover
package warts

object Any2StringAdd extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val PredefName: TermName = "Predef"
    val Any2StringAddName: TermName = "any2stringadd"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Apply(Select(Select(_, PredefName), Any2StringAddName), _) =>
            u.error(tree.pos, "Scala inserted an any2stringadd call")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
