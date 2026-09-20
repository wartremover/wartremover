package org.wartremover
package warts

/**
 * - [[https://openjdk.org/jeps/390]]
 * - [[https://github.com/scala/bug/issues/13196]]
 */
object SynchronizedValueBasedClasses extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(TypeApply(select @ Select(obj, TermName("synchronized")), _ :: Nil), _ :: Nil)
              if obj.tpe.typeSymbol.annotations.exists(_.tree.tpe.typeSymbol.fullName == "jdk.internal.ValueBased") =>
            error(u)(select.pos, "attempt to synchronize on an instance of a value-based class")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
