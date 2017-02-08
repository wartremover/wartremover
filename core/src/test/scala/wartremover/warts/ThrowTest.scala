package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Throw

class ThrowTest extends FunSuite with ResultAssertions {
  test("throw is disabled") {
    val result = WartTestTraverser(Throw) {
      def foo(n: Int): Int = throw new IllegalArgumentException("bar")
    }
    assertError(result)("[org.wartremover.warts.Throw] throw is disabled")
  }

  test("throw is disabled for non-synthetic MatchErrors") {
    val result = WartTestTraverser(Throw) {
      def foo(n: Int): Int = throw new MatchError("bar")
    }
    assertError(result)("[org.wartremover.warts.Throw] throw is disabled")
  }

  test("throw is allowed in synthetic Product.productElement") {
    val result = WartTestTraverser(Throw) {
      case class Foo(i: Int)
    }
    assertEmpty(result)
  }

  test("Throw wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Throw) {
      @SuppressWarnings(Array("org.wartremover.warts.Throw"))
      def foo(n: Int): Int = throw new IllegalArgumentException("bar")
    }
    assertEmpty(result)
  }
}
