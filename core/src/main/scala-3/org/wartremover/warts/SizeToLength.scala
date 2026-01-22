package org.wartremover
package warts

object SizeToLength extends WartTraverser {
  private val types: Set[String] = Set(
    "scala.collection.ArrayOps",
    "scala.collection.StringOps",
  )

  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "size") =>
          case t if hasWartAnnotation(t) =>
          case Select(t, "size") if t.tpe.baseClasses.exists(t => types(t.fullName)) =>
            error(tree.pos, "Maybe you should use `length` instead of `size`")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
