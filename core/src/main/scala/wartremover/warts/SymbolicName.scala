package org.wartremover
package warts

object SymbolicName extends WartTraverser {
  private val validChars = "[a-zA-Z_']+".r

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def isSymbolic(name: String) = validChars.replaceAllIn(name, "").length > 2

    new u.Traverser {
      override def traverse(tree: Tree) {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case t: DefTree if !isSynthetic(u)(t) && isSymbolic(t.symbol.name.decodedName.toString) =>
            u.error(tree.pos, "Symbolic name is disabled")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
