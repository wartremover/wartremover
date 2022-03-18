package org.wartremover
package warts

object ImplicitParameter extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: DefDef if !t.symbol.flags.is(Flags.Synthetic) =>
            val params = t.paramss.collect { case c: TermParamClause => c }
              .filter(p => (p.isImplicit || p.isGiven))
              .flatMap(_.params)
            val hasTypeParams = params.map(_.tpt.tpe).collect { case p: AppliedType => p }
            if (params.sizeIs != hasTypeParams.size) {
              error(tree.pos, "Implicit parameters are disabled")
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
