package org.wartremover
package warts

object ImplicitConversion extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: DefDef
              if t.symbol.flags.is(Flags.Implicit) &&
                !t.symbol.flags.is(Flags.Protected) &&
                !t.symbol.flags.is(Flags.Private) &&
                !t.symbol.flags.is(Flags.Synthetic) &&
                !sourceCodeContains(t, "implicit class ") &&
                t.paramss.collect { case c: TermParamClause => c }.exists(!_.isImplicit) =>
            error(tree.pos, "Implicit conversion is disabled")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
