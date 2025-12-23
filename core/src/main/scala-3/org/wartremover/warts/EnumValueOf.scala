package org.wartremover
package warts

object EnumValueOf extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "valueOf") =>
          case t if hasWartAnnotation(t) =>
          case t @ Apply(valueOf @ Select(obj, "valueOf"), arg :: Nil)
              if (t.tpe.typeSymbol.companionModule == obj.symbol) && (
                arg.tpe <:< TypeRepr.of[String]
              ) && t.tpe.typeSymbol.flags.is(Flags.Enum) && (
                valueOf.symbol.flags.is(Flags.Synthetic) || valueOf.symbol.flags.is(Flags.JavaDefined)
              ) =>
            error(tree.pos, "Enum.valueOf is disabled")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
