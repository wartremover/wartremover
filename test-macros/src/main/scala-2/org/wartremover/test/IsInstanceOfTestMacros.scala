package org.wartremover
package test

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object IsInstanceOfTestMacros {

  def is[A, B]: A => Boolean = macro IsInstanceOfTestMacrosImpl.is_impl[A, B]
}

private class IsInstanceOfTestMacrosImpl(val c: blackbox.Context) {
  def is_impl[A: c.WeakTypeTag, B: c.WeakTypeTag]: c.Expr[A => Boolean] = {
    import c.universe._

    val aTpe = weakTypeOf[A]
    val bTpe = weakTypeOf[B]
    val a = TermName(c.freshName("a"))

    c.Expr[A => Boolean](q"""
      ($a: $aTpe) => $a.isInstanceOf[$bTpe]
    """)
  }
}
