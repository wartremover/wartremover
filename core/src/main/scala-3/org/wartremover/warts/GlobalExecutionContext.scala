package org.wartremover
package warts

import scala.concurrent.ExecutionContext

object GlobalExecutionContext extends WartTraverser {
  private[wartremover] def message = "Don't use ExecutionContext.global"

  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ExecutionContext.global } | '{ ExecutionContext.Implicits.global } =>
                error(tree.pos, message)
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
