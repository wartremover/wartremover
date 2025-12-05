package org.wartremover
package warts

object PartialFunctionApply extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      val pfType = TypeRepr.of[PartialFunction].typeSymbol
      val seqType = TypeRepr.of[scala.collection.Seq].typeSymbol
      val mapType = TypeRepr.of[scala.collection.Map].typeSymbol
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case Apply(Select(obj, "apply"), _ :: Nil)
              if obj.tpe.derivesFrom(pfType) && !obj.tpe.derivesFrom(seqType) && !obj.tpe.derivesFrom(mapType) =>
            error(tree.pos, "PartialFunction#apply is disabled")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
