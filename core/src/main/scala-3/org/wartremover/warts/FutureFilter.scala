package org.wartremover
package warts

object FutureFilter extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if Seq("filter", "withFilter", "for").forall(s => sourceCodeNotContains(tree, s)) =>
          case _ if hasWartAnnotation(tree) =>
          case Apply(Select(obj, methodName @ ("filter" | "withFilter")), _ :: Nil)
              if obj.tpe.typeSymbol.fullName == "scala.concurrent.Future" =>
            error(tree.pos, s"Future.${methodName} is disabled")
            super.traverseTree(tree)(owner)
          case Apply(
                TypeApply(
                  Select(
                    Apply(
                      Apply(
                        Select(
                          obj,
                          "withFilter"
                        ),
                        _
                      ),
                      _
                    ),
                    _
                  ),
                  _
                ),
                _
              ) if obj.tpe.typeSymbol.fullName == "scala.concurrent.Future" =>
            error(tree.pos, "Future.withFilter is disabled")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
