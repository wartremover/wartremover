package org.wartremover
package warts

object ArrayToString extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new u.Traverser {
      private val arrayType = rootMirror.staticClass("scala.Array").toTypeConstructor
      private val S = TermName("s")
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case Select(array, TermName("toString")) if array.tpe.dealias.typeConstructor =:= arrayType =>
            error(u)(tree.pos, "Array.toString is disabled")
          case Apply(Select(stringContext, S), args) if (stringContext.tpe.dealias <:< typeOf[StringContext]) =>
            args.withFilter(_.tpe.dealias.typeConstructor =:= arrayType).foreach { a =>
              error(u)(a.pos, "Array.toString is disabled")
            }
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
