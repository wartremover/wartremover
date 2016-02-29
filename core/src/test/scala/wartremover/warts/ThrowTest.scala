package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Throw

class ThrowTest extends FunSuite {
  test("throw is disabled") {
    val result = WartTestTraverser(Throw) {
      def foo(n: Int): Int = throw new IllegalArgumentException("bar")
    }
    assertResult(List("throw is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("throw is disabled for non-synthetic MatchErrors") {
    val result = WartTestTraverser(Throw) {
      def foo(n: Int): Int = throw new MatchError("bar")
    }
    assertResult(List("throw is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("throw is allowed in synthetic Product.productElement") {
    val result = WartTestTraverser(Throw) {
      case class Foo(i: Int)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Throw wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Throw) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
      def foo(n: Int): Int = throw new IllegalArgumentException("bar")
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
