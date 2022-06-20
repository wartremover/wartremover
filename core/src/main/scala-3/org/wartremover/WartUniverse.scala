package org.wartremover

import dotty.tools.dotc.ast.tpd
import scala.annotation.nowarn
import scala.quoted.Exprs
import scala.quoted.Quotes
import scala.quoted.Type
import scala.quoted.Varargs
import java.lang.SuppressWarnings

object WartUniverse {
  type Aux[X <: Quotes] = WartUniverse { type Q = X }

  def apply[Q <: Quotes](onlyWarning: Boolean, logLevel: LogLevel, quotes: Q): Aux[Q] = {
    type X = Q
    val q = quotes
    new WartUniverse(onlyWarning, logLevel) {
      override type Q = X
      override val quotes = q
    }
  }
}

abstract class WartUniverse(onlyWarning: Boolean, logLevel: LogLevel) { self =>
  type Q <: Quotes
  val quotes: Q

  import quotes.reflect.{report => _, *}

  protected def onWarn(msg: String, pos: Position): Unit =
    quotes.reflect.report.warning(msg = msg, pos = pos)

  protected def onError(msg: String, pos: Position): Unit =
    quotes.reflect.report.error(msg = msg, pos = pos)

  abstract class Traverser(traverser: WartTraverser) extends TreeTraverser {
    final implicit val q: self.quotes.type = self.quotes

    private[this] def withPrefix(name: String): String = s"[wartremover:${name}] "

    protected final def warning(pos: Position, message: String): Unit =
      onWarn(msg = withPrefix(traverser.simpleName) + message, pos = pos)

    protected final def error(pos: Position, message: String): Unit = {
      if (onlyWarning) {
        warning(pos, message)
      } else {
        val msg = withPrefix(traverser.simpleName) + message
        onError(msg = msg, pos = pos)
      }
    }

    def hasWartAnnotation(t: Tree): Boolean = {
      hasWartAnnotationSymbol(t.symbol) || Option(t.symbol.maybeOwner)
        .filterNot(_.isNoSymbol)
        .filter(s => s.isClassDef || s.isValDef || s.isDefDef)
        .exists(hasWartAnnotationSymbol)
    }

    protected[this] def hasWartAnnotationSymbol(s: Symbol): Boolean = {
      val SuppressWarningsSymbol = TypeTree.of[SuppressWarnings].symbol

      val args: Set[String] = s
        .getAnnotation(SuppressWarningsSymbol)
        .collect {
          case Apply(
                Select(_, "<init>"),
                Apply(Apply(_, Typed(Repeated(values, _), _) :: Nil), Apply(_, _ :: Nil) :: Nil) :: Nil
              ) =>
            // "-Yexplicit-nulls"
            // https://github.com/wartremover/wartremover/issues/660
            values.collect { case Literal(StringConstant(str)) =>
              str
            }
        }
        .toList
        .flatten
        .toSet

      args(traverser.fullName) || args("org.wartremover.warts.All")
    }

    def sourceCodeContains(t: Tree, src: String): Boolean = {
      // avoid StringIndexOutOfBoundsException
      // Don't use `def sourceCode: Option[String]`
      // https://github.com/lampepfl/dotty/issues/14785
      // https://github.com/lampepfl/dotty/blob/58b59a5f88508bb4b3/compiler/src/scala/quoted/runtime/impl/QuotesImpl.scala#L2791-L2793
      t.pos.sourceFile.content.exists { content =>
        val sliced = content.slice(t.pos.start, t.pos.end)
        sliced.contains(src)
      }
    }

    def getSyntheticPartialFunction(tree: Tree): Option[ClassDef] = {
      PartialFunction.condOpt(tree) {
        case c: ClassDef
            if c.symbol.flags.is(Flags.Synthetic) && c.parents.collect { case t: TypeTree =>
              t.tpe
            }.exists(
              _ <:< TypeRepr.of[PartialFunction[Nothing, Any]]
            ) =>
          c
      }
    }

    def isPrimitive(t: TypeRepr): Boolean = {
      t <:< TypeRepr.of[Boolean] ||
      t <:< TypeRepr.of[Byte] ||
      t <:< TypeRepr.of[Short] ||
      t <:< TypeRepr.of[Char] ||
      t <:< TypeRepr.of[Int] ||
      t <:< TypeRepr.of[Long] ||
      t <:< TypeRepr.of[Float] ||
      t <:< TypeRepr.of[Double]
    }

    @nowarn("msg=dotty.tools.dotc.ast.tpd")
    override def foldOverTree(x: Unit, tree: Tree)(owner: Symbol): Unit = {
      try {
        tree match {
          case _: tpd.Template =>
          case _: tpd.Typed =>
          case _: tpd.InlineMatch => // https://github.com/lampepfl/dotty/issues/14789
          case _ =>
            super.foldOverTree(x, tree)(owner)
        }
      } catch {
        case e: MatchError =>
          if (logLevel != LogLevel.Disable) {
            warning(tree.pos, s"MatchError ${tree.getClass} ${owner.getClass}")
          }
      }
    }
  }

}
