package org.wartremover
package warts

object ArrayEquals extends WartTraverser {
  private val types: Set[String] = Set(
    "scala.Array",
    "scala.collection.Iterator",
  )

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "==") =>
          case t if hasWartAnnotation(t) =>
          case Apply(Select(x1, "=="), Literal(NullConstant()) :: Nil) =>
            super.traverseTree(x1)(owner)
          case Apply(s @ Select(x1, "=="), _ :: Nil) if x1.tpe.baseClasses.exists(t => types(t.fullName)) =>
            error(selectNamePosition(s), "== is disabled")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
