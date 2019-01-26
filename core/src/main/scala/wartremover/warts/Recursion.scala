package org.wartremover
package warts

object Recursion extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    new u.Traverser {
      val Tailrec = typeOf[scala.annotation.tailrec]
      var open: Set[Symbol] = Set()
      def inside[A](sym: Symbol)(expr: => A): A = {
        open = open + sym
        try expr finally open = open - sym
      }
      def isTailrec(m: Symbol) = m.annotations exists (_.tree.tpe <:< Tailrec)
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t)      =>
          case t: DefDef if !isTailrec(t.symbol) => inside(tree.symbol)(super.traverse(tree))
          case t: RefTree if open(tree.symbol)   => error(u)(t.pos, "Unmarked recursion") ; super.traverse(tree)
          case _                                 => super.traverse(tree)
        }
      }
    }
  }
}
