package org.wartremover
package warts

object TripleQuestionMark extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val TripleQuestionMarkName = TermName("$qmark$qmark$qmark")
    val predefSymbol = rootMirror.staticModule("scala.Predef")

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(left, TripleQuestionMarkName) if left.symbol == predefSymbol =>
            error(u)(tree.pos, "??? is disabled")
          // TODO: This ignores a lot
          case LabelDef(_, _, rhs) if isSynthetic(u)(tree) =>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
