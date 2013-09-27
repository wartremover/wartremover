package org.brianmckenna.wartremover
package warts

object EitherProjectionPartial extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val leftProjectionSymbol = rootMirror.staticClass("scala.util.Either.LeftProjection")
    val rightProjectionSymbol = rootMirror.staticClass("scala.util.Either.RightProjection")
    val GetName: TermName = "get"
    new Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case Select(left, GetName) if left.tpe.baseType(leftProjectionSymbol) != NoType =>
            u.error(tree.pos, "LeftProjection#get is disabled - use LeftProjection#toOption instead")
          case Select(left, GetName) if left.tpe.baseType(rightProjectionSymbol) != NoType =>
            u.error(tree.pos, "RightProjection#get is disabled - use RightProjection#toOption instead")
          case _ =>
        }
        super.traverse(tree)
      }
    }
  }
}
