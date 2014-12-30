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
      val excludes: List[String] = List.empty // TODO: find a sensible way to initialize this field with useful data
    }

    apply(MacroUniverse).traverse(expr.tree)

    expr
  }

  def asAnnotationMacro(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val block = Block(annottees.map(_.tree).toList, Literal(Constant(())))
    c.typeCheck(block)

    annottees.foreach { expr =>
      asMacro(c)(expr)
    }

    c.Expr[Any](block)
  }

  def compose(o: WartTraverser): WartTraverser = new WartTraverser {
    def apply(u: WartUniverse): u.Traverser = {
      new u.Traverser {
        override def traverse(tree: u.universe.Tree): Unit = {
          WartTraverser.this(u).traverse(tree)
          o(u).traverse(tree)
        }
      }
    }
  }

  def isSynthetic(u: WartUniverse)(t: u.universe.Tree): Boolean =
    if(t.symbol != null)
      t.symbol.isSynthetic
    else
      false

  def wasInferred(u: WartUniverse)(t: u.universe.TypeTree): Boolean =
    t.original == null
}

object WartTraverser {
  def sumList(u: WartUniverse)(l: List[WartTraverser]): u.Traverser =
    l.reduceRight(_ compose _)(u)
}

trait WartUniverse {
  val universe: Universe
  type Traverser = universe.Traverser
  type TypeTag[T] = universe.TypeTag[T]
  def error(pos: universe.Position, message: String): Unit
  def warning(pos: universe.Position, message: String): Unit
}
