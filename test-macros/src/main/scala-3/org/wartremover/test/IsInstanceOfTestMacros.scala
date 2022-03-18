package org.wartremover
package test

import scala.quoted.Expr
import scala.quoted.Quotes
import scala.quoted.Type

object IsInstanceOfTestMacros {

  inline def is[A, B]: A => Boolean = ${ is_impl[A, B] }

  private[this] def is_impl[A: Type, B: Type](using Quotes): Expr[A => Boolean] =
    '{ (a: A) => a.isInstanceOf[B] }
}
