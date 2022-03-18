package org.wartremover
package warts

object DefaultArguments extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t @ DefDef(name, _, _, _)
              if (name != "copy") &&
                t.termParamss
                  .flatMap(_.params)
                  .exists(p =>
                    p.symbol.flags.is(Flags.HasDefault) &&
                      !p.symbol.flags.is(Flags.Synthetic)
                  ) =>
            error(tree.pos, "Function has default arguments")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
