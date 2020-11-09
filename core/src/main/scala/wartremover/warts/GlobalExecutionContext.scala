package org.wartremover
package warts

object GlobalExecutionContext extends WartTraverser {
  private[wartremover] def message = "Don't use ExecutionContext.global"

  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case q"scala.concurrent.ExecutionContext.Implicits.global" | q"scala.concurrent.ExecutionContext.global" =>
            error(u)(tree.pos, message)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
