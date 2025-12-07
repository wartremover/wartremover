package org.wartremover
package warts

object FutureFilter extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val futureType = rootMirror.staticClass("scala.concurrent.Future").toTypeConstructor
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Apply(Select(obj, TermName(methodName @ ("filter" | "withFilter"))), _ :: Nil)
              if obj.tpe.typeConstructor <:< futureType =>
            error(u)(tree.pos, s"Future.${methodName} is disabled")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
