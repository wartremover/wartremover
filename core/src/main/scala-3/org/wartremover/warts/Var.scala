package org.wartremover
package warts

object Var extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      private def notXmlTypes(t: ValDef): Boolean = {
        !t.tpt.tpe.typeSymbol.fullName.startsWith("scala.xml.")
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: ValDef if t.symbol.flags.is(Flags.Mutable) && !t.symbol.flags.is(Flags.Synthetic) && notXmlTypes(t) =>
            error(t.pos, "var is disabled")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
