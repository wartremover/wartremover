package org.wartremover

import tools.nsc.Global
import reflect.api.Universe
import reflect.macros.blackbox.Context
import scala.util.matching.Regex

trait WartTraverser {
  def apply(u: WartUniverse): u.Traverser

  lazy val className = this.getClass.getName.stripSuffix("$")
  lazy val wartName = className.substring(className.lastIndexOf('.') + 1)

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
    c.typecheck(block)

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

  def isSyntheticPartialFunction(u: WartUniverse)(tree: u.universe.Tree): Boolean = {
    import u.universe._
    tree match {
      case ClassDef(_, className, _, Template((parents, _, _))) =>
        isAnonymousFunctionName(u)(className) && isSynthetic(u)(tree) && parents.exists {
          case t @ TypeTree() =>
            t.symbol.fullName == "scala.runtime.AbstractPartialFunction"
          case _ =>
            false
        }
      case _ =>
        false
    }
  }

  def isAnonymousFunctionName(u: WartUniverse)(t: u.universe.TypeName): Boolean =
    t == u.universe.TypeName("$anonfun")

  def isSynthetic(u: WartUniverse)(t: u.universe.Tree): Boolean = {
    // Unfortunately, Scala does not mark accessors as Synthetic (even though the documentation claims that they do).
    // A manually crafted getter/setter does not deserve a GETTER/SETTER flag in Scala compiler's eyes, so we can
    // "safely" rely on this
    Option(t.symbol).map(s => s.isSynthetic || s.isImplementationArtifact || (s.isTerm && s.asTerm.isAccessor)).getOrElse(false)
  }

  def isPrimitive(u: WartUniverse)(t: u.universe.Type): Boolean =
    t <:< u.universe.typeOf[Boolean] ||
    t <:< u.universe.typeOf[Byte] ||
    t <:< u.universe.typeOf[Short] ||
    t <:< u.universe.typeOf[Char] ||
    t <:< u.universe.typeOf[Int] ||
    t <:< u.universe.typeOf[Long] ||
    t <:< u.universe.typeOf[Float] ||
    t <:< u.universe.typeOf[Double]


  def hasTypeAscription(u: WartUniverse)(tree: u.universe.ValOrDefDef): Boolean = {
    import u.universe._
    tree.tpt.nonEmpty && (tree.tpt match {
      case t@TypeTree() => !wasInferred(u)(t)
      case _ => false
    })
  }

  private def hasAccess(u: WartUniverse)(t: u.universe.ValOrDefDef, p: u.universe.Symbol => Boolean): Boolean = {
    // If this a field, then look at the getter's visibility
    val symbol = if(!t.symbol.isMethod && (t.symbol.owner.isType || t.symbol.owner.isModule)) {
      t.symbol.asTerm.getter
    } else t.symbol

    p(symbol)
  }

  def isPublic(u: WartUniverse)(t: u.universe.ValOrDefDef): Boolean = {
    hasAccess(u)(t, s => s.isPublic && (s != u.universe.NoSymbol))
  }

  def isPrivate(u: WartUniverse)(t: u.universe.ValOrDefDef): Boolean = {
    hasAccess(u)(t, s => s.isPrivate)
  }

  def wasInferred(u: WartUniverse)(t: u.universe.TypeTree): Boolean =
    t.original == null

  def isWartAnnotation(u: WartUniverse)(a : u.universe.Annotation) : Boolean = {
    import u.universe._
    a.tree.tpe <:< typeTag[java.lang.SuppressWarnings].tpe &&
      a.tree.children.tail.exists {
        _.exists {
          case Literal(Constant(arg)) =>
            (arg == className) || (arg == "org.wartremover.warts.All")
          case _ =>
            false
        }
      }
  }

  def hasWartAnnotation(u: WartUniverse)(tree: u.universe.Tree) = {
    import u.universe._
    tree match {
      case t: ValOrDefDef =>
        t.symbol.annotations.exists(isWartAnnotation(u)) ||
          (t.symbol != null && t.symbol.isTerm && t.symbol.asTerm.isAccessor &&
            t.symbol.asTerm.accessed.annotations.exists(isWartAnnotation(u)))
      case t: ImplDef => t.symbol.annotations.exists(isWartAnnotation(u))
      case t => false
    }
  }

  def error(u: WartUniverse)(pos: u.universe.Position, message: String): Unit = u.error(pos, message, wartName)

  def warning(u: WartUniverse)(pos: u.universe.Position, message: String): Unit = u.warning(pos, message, wartName)
}

object WartTraverser {
  def sumList(u: WartUniverse)(l: List[WartTraverser]): u.Traverser =
    l.reduceRight(_ compose _)(u)
}

trait WartUniverse {
  val universe: Universe
  type Traverser = universe.Traverser
  type TypeTag[T] = universe.TypeTag[T]
  protected def error(pos: universe.Position, message: String): Unit
  protected def warning(pos: universe.Position, message: String): Unit
  def error(pos: universe.Position, message: String, wartName: String): Unit =
    error(pos, s"[wartremover:$wartName] $message")
  def warning(pos: universe.Position, message: String, wartName: String): Unit =
    warning(pos, s"[wartremover:$wartName] $message")
}
