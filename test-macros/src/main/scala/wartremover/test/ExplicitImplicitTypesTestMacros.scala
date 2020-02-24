package wartremover.test

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ExplicitImplicitTypesTestMacros {

  def valsWithoutTypeAscription: Any = macro ExplicitImplicitTypesTestMacros.valsWithoutTypeAscriptionImpl
  def valsWithTypeAscription: Any = macro ExplicitImplicitTypesTestMacros.valsWithTypeAscriptionImpl
  def defsWithoutTypeAscription: Any = macro ExplicitImplicitTypesTestMacros.defsWithoutTypeAscriptionImpl
  def defsWithTypeAscription: Any = macro ExplicitImplicitTypesTestMacros.defsWithTypeAscriptionImpl
}

private class ExplicitImplicitTypesTestMacros(val c: blackbox.Context) {

  import c.universe._

  def valsWithoutTypeAscriptionImpl: c.Expr[Any] = {
    val tree = q"""
      implicit val foo = 5
      implicit var bar = 5
      """
    c.Expr(tree)
  }

  def valsWithTypeAscriptionImpl: c.Expr[Any] = {
    val tree = q"""
      implicit val foo: Int = 5
      implicit var bar: Int = 5
      """
    c.Expr(tree)
  }

  def defsWithoutTypeAscriptionImpl: c.Expr[Any] = {
    val tree = q"""
      implicit def foo = 5
      implicit def bar[A] = 5
      implicit def baz(i: Int) = 5
      implicit def qux[I](i: I) = 5
      """
    c.Expr(tree)
  }

  def defsWithTypeAscriptionImpl: c.Expr[Any] = {
    val tree = q"""
      implicit def foo: Int = 5
      implicit def bar[A]: Int = 5
      implicit def baz(i: Int): Int = 5
      implicit def qux[I](i: I):Int = 5
      """
    c.Expr(tree)
  }
}

