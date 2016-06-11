package org.brianmckenna.wartremover
package warts

object Overloading extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: ClassDef if isSynthetic(u)(t) =>
          case t: ClassDef if t.symbol.info.decls.nonEmpty =>
            val overloaded = t.symbol.info.members
                .filterNot(_.annotations.exists(isWartAnnotation(u)))
                .map(_.name.decodedName.toString)
                .groupBy(identity)
                .filter(_._2.size > 1)
                .keys
            t.symbol.info.decls
                .filter(decl => overloaded.exists(_ == decl.name.decodedName.toString))
                .foreach(delc => u.error(delc.pos, "Overloading is disabled"))
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
