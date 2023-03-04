package org.wartremover
package test

import scala.quoted.Expr
import scala.quoted.Quotes
import org.wartremover.warts.AsInstanceOf
import org.scalatest.funsuite.AnyFunSuite

class AsInstanceOfTest2 extends AnyFunSuite with ResultAssertions {
  test("match Expr") {
    val result = WartTestTraverser(AsInstanceOf) {
      def foo[A](e: Expr[A])(using Quotes): Boolean = {
        e match {
          case '{ $x1: t1 } => true
          case _ => false
        }
      }
    }
    assertEmpty(result)
  }
}
