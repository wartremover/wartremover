package org.wartremover

import dotty.tools.dotc.ast.tpd
import scala.annotation.nowarn
import scala.quoted.Quotes
import scala.quoted.Type
import java.lang.SuppressWarnings

class WartUniverse(onlyWarning: Boolean, logLevel: LogLevel, val quotes: Quotes) { self =>
  import quotes.reflect.*
  abstract class Traverser(traverser: WartTraverser) extends TreeTraverser {
    final implicit val q: self.quotes.type = self.quotes

    private[this] def withPrefix(name: String): String = s"[wartremover:${name}] "

    protected final def warning(pos: Position, message: String): Unit =
      report.warning(msg = withPrefix(traverser.simpleName) + message, pos = pos)

    protected final def error(pos: Position, message: String): Unit = {
      if (onlyWarning) {
        warning(pos, message)
      } else {
        val msg = withPrefix(traverser.simpleName) + message
        report.error(msg = msg, pos = pos)
      }
    }

    def hasWartAnnotation(t: Tree): Boolean = {
      hasWartAnnotationSymbol(t.symbol) || Option(t.symbol.maybeOwner)
        .filterNot(_.isNoSymbol)
        .filter(s => s.isClassDef || s.isValDef || s.isDefDef)
        .exists(hasWartAnnotationSymbol)
    }

    private[this] def hasWartAnnotationSymbol(s: Symbol): Boolean = {
      val SuppressWarningsSymbol = TypeTree.of[SuppressWarnings].symbol

      val args: Set[String] = s
        .getAnnotation(SuppressWarningsSymbol)
        .collect {
          case a1 if a1.isExpr =>
            PartialFunction
              .condOpt(a1.asExpr) { case '{ new SuppressWarnings($a2: Array[String]) } =>
                PartialFunction
                  .condOpt(a2.asTerm) { case Apply(Apply(_, Typed(e, _) :: Nil), _) =>
                    e.asExprOf[Seq[String]].value
                  }
                  .flatten
              }
              .flatten
        }
        .flatten
        .toList
        .flatten
        .toSet

      args(traverser.fullName) || args("org.wartremover.warts.All")
    }

    def sourceCodeContains(t: Tree, src: String): Boolean = {
      // avoid StringIndexOutOfBoundsException
      // Don't use `def sourceCode: Option[String]`
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
