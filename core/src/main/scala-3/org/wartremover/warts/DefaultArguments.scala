package org.wartremover
package warts

object DefaultArguments extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: DefDef if !t.symbol.flags.is(Flags.Synthetic) =>
            t.termParamss
              .flatMap(_.params)
              .find(p =>
                p.symbol.flags.is(Flags.HasDefault) &&
                  !p.symbol.flags.is(Flags.Synthetic)
              ) match {
              case Some(p) =>
                error(p.pos, "Function has default arguments")
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
