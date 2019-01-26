package org.wartremover
package warts

object EitherProjectionPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val leftProjectionSymbol = rootMirror.staticClass("scala.util.Either.LeftProjection")
    val rightProjectionSymbol = rootMirror.staticClass("scala.util.Either.RightProjection")
    val GetName = TermName("get")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(left, GetName) if left.tpe.baseType(leftProjectionSymbol) != NoType =>
            error(u)(tree.pos, "LeftProjection#get is disabled - use LeftProjection#toOption instead")
            super.traverse(tree)
          case Select(left, GetName) if left.tpe.baseType(rightProjectionSymbol) != NoType =>
            error(u)(tree.pos, "RightProjection#get is disabled - use RightProjection#toOption instead")
            super.traverse(tree)
          case _ => super.traverse(tree)
        }
      }
    }
  }
}
