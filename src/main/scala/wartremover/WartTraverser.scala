package org.brianmckenna.wartremover

import tools.nsc.Global
import reflect.api.Universe
import reflect.macros.Context

trait WartTraverser {
  def apply(u: WartUniverse): u.Traverser

  def asMacro(c: Context)(expr: c.Expr[Any]): c.Expr[Any] = {
    import c.universe._

    object MacroUniverse extends WartUniverse {
      val universe: c.universe.type = c.universe
      def error(pos: universe.Position, message: String) = c.error(pos, message)
      def warning(pos: universe.Position, message: String) = c.warning(pos, message)
    }

    apply(MacroUniverse).traverse(expr.tree)

    expr
  }

  def compose(o: WartTraverser): WartTraverser = new WartTraverser {
    def apply(u: WartUniverse): u.Traverser = {
      new u.Traverser {
        override def traverse(tree: u.universe.Tree) {
          WartTraverser.this(u).traverse(tree)
          o(u).traverse(tree)
        }
      }
    }
  }
}

object WartTraverser {
  def sumList(u: WartUniverse)(l: List[WartTraverser]): u.Traverser =
    l.reduceRight(_ compose _)(u)
}

trait WartUniverse {
  val universe: Universe
  type Traverser = universe.Traverser
  def error(pos: universe.Position, message: String)
  def warning(pos: universe.Position, message: String)
}
