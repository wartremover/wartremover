package org.wartremover

import tools.nsc.Global
import reflect.api.Universe
import reflect.macros.Context
import scala.util.Try

trait WartTraverser {
  def apply(u: WartUniverse): u.Traverser

  lazy val className = this.getClass.getName.stripSuffix("$")

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

  def isWartAnnotation(u: WartUniverse)(a : u.universe.Annotation) : Boolean = {
    import u.universe._
    a.tpe <:< typeTag[java.lang.SuppressWarnings].tpe &&
      a.javaArgs.exists {
        case Tuple2(_, ArrayArgument(args)) => args.exists {
          case LiteralArgument(Constant(arg)) => arg == className
        }
        case _ => false
      }
  }

  def hasWartAnnotation(u: WartUniverse)(tree: u.universe.Tree) = {
    import u.universe._
    tree match {
      case t: ValOrDefDef => t.symbol.annotations.exists(isWartAnnotation(u))
      case t: ImplDef => t.symbol.annotations.exists(isWartAnnotation(u))
      case t => false
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
  type TypeTag[T] = universe.TypeTag[T]
  def error(pos: universe.Position, message: String): Unit
  def warning(pos: universe.Position, message: String): Unit
}
