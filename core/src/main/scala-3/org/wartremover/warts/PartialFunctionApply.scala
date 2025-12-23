package org.wartremover
package warts

object PartialFunctionApply extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      val pfType = TypeRepr.of[PartialFunction].typeSymbol
      val seqType = TypeRepr.of[scala.collection.SeqOps].typeSymbol
      val mapType = TypeRepr.of[scala.collection.MapOps].typeSymbol
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case If(
                Apply(Select(x1, "isDefinedAt"), x2 :: Nil),
                Apply(Select(x3, "apply"), x4 :: Nil),
                elseTerm
              ) if (x1.symbol == x3.symbol) && (x2.symbol == x4.symbol) =>
            super.traverseTree(elseTerm)(owner)
          case CaseDef(
                _,
                Some(Apply(Select(x1, "isDefinedAt"), x2 :: Nil)),
                Block(Nil, Apply(Select(x3, "apply"), x4 :: Nil)),
              ) if (x1.symbol == x3.symbol) && (x2.symbol == x4.symbol) =>
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
