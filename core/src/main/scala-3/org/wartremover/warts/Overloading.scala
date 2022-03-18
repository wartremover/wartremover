package org.wartremover
package warts

object Overloading extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: ClassDef =>
            // TODO
            val parentMethodNames: Set[String] = (
              t.parents.flatMap(_.symbol.methodMembers) ++
                TypeRepr.of[AnyRef].classSymbol.toList.flatMap(_.methodMembers)
            ).map(_.name).toSet

            val methods = t.body.collect { case d: DefDef => d }
            val overloads = methods
              .groupBy(_.name)
              .map(_._2.filterNot(t => hasWartAnnotation(t)).filterNot(_.symbol.flags.is(Flags.Override)))
              .filter(f => (f.sizeIs > 1))

            overloads.flatten.foreach { method =>
              error(method.pos, "Overloading is disabled")
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
