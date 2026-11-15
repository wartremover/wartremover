package org.wartremover
package warts

/**
 * [[https://github.com/scala/scala3/pull/26794]]
 */
object CatchThrowable extends WartTraverser {
  private def message: String = "Catching Throwable can lead to unexpected behavior"

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "Throwable") =>
          case t if hasWartAnnotation(t) =>
          case t: Try =>
            t.cases.iterator.map(_.pattern).foreach {
              case p: Typed if p.tpt.tpe =:= TypeRepr.of[Throwable] =>
                error(p.pos, message)
              case Bind(_, p: Typed) if p.tpt.tpe =:= TypeRepr.of[Throwable] =>
                error(p.pos, message)
              case _ =>
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
