package org.wartremover
package test

import language.experimental.macros
import reflect.internal.util.Position
import reflect.macros.blackbox.Context
import scala.reflect.NameTransformer
import tools.nsc.reporters.Reporter

object WartTestTraverser {
  case class Result(errors: List[String], warnings: List[String])

  private[this] def getWartTraverserObjectOption(name: String): Option[WartTraverser] =
    try {
      Some(getWartTraverserObject(name))
    } catch {
      case e: Throwable =>
        None
    }

  private[this] def getWartTraverserObject(name: String): WartTraverser = {
    val clazz = Class.forName(name + NameTransformer.MODULE_SUFFIX_STRING)
    val field = clazz.getField(NameTransformer.MODULE_INSTANCE_NAME)
    val instance = field.get(null)
    instance.asInstanceOf[WartTraverser]
  }

  def apply(t: WartTraverser)(a: Any): Result = macro applyImpl
  def applyImpl(c: Context)(t: c.Expr[WartTraverser])(a: c.Expr[Any]) = {
    import c.universe._

    val traverser =
      getWartTraverserObjectOption(t.tree.toString).getOrElse {
        try {
          val name = t.tree.tpe.typeSymbol.fullName
          getWartTraverserObject(name)
        } catch {
          case e: Throwable =>
            // eval is too slow
            c.eval[WartTraverser](c.Expr(c.untypecheck(t.tree.duplicate)))
        }
      }

    val errors = collection.mutable.ListBuffer[String]()
    val warnings = collection.mutable.ListBuffer[String]()

    object MacroTestUniverse extends WartUniverse {
      val universe: c.universe.type = c.universe
      def error(pos: universe.Position, message: String) = errors += message
      def warning(pos: universe.Position, message: String) = warnings += message
    }

    traverser(MacroTestUniverse).traverse(a.tree)

    c.Expr(
      q"_root_.org.wartremover.test.WartTestTraverser.Result(_root_.scala.List(..${errors.toList}), _root_.scala.List(..${warnings.toList}))"
    )
  }

  /**
   * Example: {{{
   * val result = WartTestTraverser.applyToFiles(Return)(
   *   "src/test/resources/A.scala",
   *   "src/test/resources/B.scala"
   * )}}}
   * @param t traverser to test
   * @param paths test source files
   * @return result of applying {@code t} to files at {@code paths}
   */
  def applyToFiles(t: WartTraverser)(paths: String*): Result = {
    var errors = List[String]()
    var warnings = List[String]()
    Main.compile(
      Main.WartArgs(List(t.className), paths.toList, Nil),
      Some(new Reporter {
        override protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean) = severity match {
          case ERROR => errors ::= msg
          case WARNING => warnings ::= msg
          case _ =>
        }
      })
    )
    Result(errors, warnings)
  }
}
