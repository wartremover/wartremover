package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.ImplicitParameter

class ImplicitParameterTest extends FunSuite with ResultAssertions {

  test("Implicit parameters are disabled") {
    val result = WartTestTraverser(ImplicitParameter) {
      def f()(implicit s: String) = ()

      def f2[A]()(implicit a: A) = ()
    }
    assertErrors(result)("[org.wartremover.warts.ImplicitParameter] Implicit parameters are disabled", 2)
  }

  test("Context bounds are enabled") {
    val result = WartTestTraverser(ImplicitParameter) {
      def f[A: Seq]() = ()
    }
    assertEmpty(result)
  }

  test("Desugared context bounds are enabled") {
    val result = WartTestTraverser(ImplicitParameter) {
      def f[A, B](implicit ev: Either[A, _], ev2: Either[_, Seq[B]]) = ()
    }
    assertEmpty(result)
  }

  test("Defs without parameters don't lead to a crash") {
    val result = WartTestTraverser(ImplicitParameter) {
      def f = ()
    }
    assertEmpty(result)
  }

  test("ImplicitParameter wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ImplicitParameter) {
      @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
      def f()(implicit s: String) = ()
    }
    assertEmpty(result)
  }

}
