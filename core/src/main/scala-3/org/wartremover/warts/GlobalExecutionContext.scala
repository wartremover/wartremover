package org.wartremover
package warts

object GlobalExecutionContext extends WartTraverser {
  private[wartremover] def message = "Don't use ExecutionContext.global"

  private val globalNames: Set[String] = Set(
    "scala.concurrent.ExecutionContext$.Implicits$.global",
    "scala.concurrent.ExecutionContext$.global",
  )

  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case _ if globalNames(tree.symbol.fullName) =>
            error(tree.pos, message)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
