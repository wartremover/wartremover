package org.wartremover
package warts

object Enumeration extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val enumeration = typeOf[scala.Enumeration].typeSymbol

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: ImplDef if t.symbol.typeSignature.baseClasses.contains(enumeration) =>
            error(u)(tree.pos, "Enumeration is disabled - use case objects instead")
          case t => super.traverse(tree)
        }
      }
    }
  }
}
