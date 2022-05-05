package org.wartremover
package warts

object Recursion extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private[this] val Tailrec = TypeRepr.of[scala.annotation.tailrec]
      private[this] var open: Set[Symbol] = Set.empty[Symbol]
      private def inside[A](sym: Symbol)(expr: => A): A = {
        open = open + sym
        try expr
        finally open = open - sym
      }
      private def isTailrec(m: Symbol): Boolean = m.annotations.exists(_.tpe <:< Tailrec)
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if hasWartAnnotation(tree) =>
          case t: DefDef if !isTailrec(t.symbol) =>
            inside(tree.symbol)(super.traverseTree(tree)(owner))
          case t: Ref if open(tree.symbol) =>
            error(t.pos, "Unmarked recursion")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
