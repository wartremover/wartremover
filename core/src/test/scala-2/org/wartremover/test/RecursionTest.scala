package org.wartremover
package test

import scala.annotation.tailrec
import org.wartremover.warts.Recursion
import org.scalatest.funsuite.AnyFunSuite

// TODO Scala 3 ?
class RecursionTest extends AnyFunSuite with ResultAssertions {

  test("can't use recursion") {
    val result = WartTestTraverser(Recursion) {
      def foo(x: Int): Int = foo(x)
    }
    assertError(result)("Unmarked recursion")
  }

  test("can't use nested recursion") {
    val result = WartTestTraverser(Recursion) {
      def foo(x: Int): Int = {
        def bar(y: Int): Int = foo(y)
        bar(1)
      }
    }
    assertError(result)("Unmarked recursion")
  }

  test("can use non-recursion") {
    val result = WartTestTraverser(Recursion) {
      def foo(x: Int): Int = x
    }
    assertEmpty(result)
  }

  test("can use tail recursion") {
    val result = WartTestTraverser(Recursion) {
      @tailrec def foo(x: Int): Int = if (x < 0) 1 else foo(x - 1)
    }
    assertEmpty(result)
  }

  test("Recursion wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Recursion) {
      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      def foo(x: Int): Int = foo(x)
    }
    assertEmpty(result)
  }
}
