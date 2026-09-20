package org.wartremover
package warts

/**
 * - [[https://openjdk.org/jeps/390]]
 * - [[https://github.com/scala/scala3/issues/27110]]
 */
object SynchronizedValueBasedClasses extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case _ if sourceCodeNotContains(tree, "synchronized") =>
          case Apply(TypeApply(select @ Select(obj, "synchronized"), _ :: Nil), _ :: Nil)
              if obj.tpe.typeSymbol.annotations.exists(_.tpe.typeSymbol.fullName == "jdk.internal.ValueBased") =>
            error(select.pos, "attempt to synchronize on an instance of a value-based class")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
