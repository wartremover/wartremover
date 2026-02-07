package org.wartremover
package warts

object ArrayToString extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case s @ Select(array, "toString") =>
            array.tpe.dealias.asType match {
              case '[Array[_]] =>
                error(selectNamePosition(s), "Array.toString is disabled")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case Apply(s, Typed(Repeated(args, _), _) :: Nil) if s.symbol.fullName == "scala.StringContext.s" =>
            args
              .map(a => (a, a.tpe.dealias.asType))
              .collect { case (a, '[Array[_]]) =>
                a
              }
              .foreach { a =>
                error(a.pos, "Array.toString is disabled")
              }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
