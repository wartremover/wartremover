package org.wartremover
package test

import org.scalatest.FunSuite
import scala.annotation.tailrec
import org.wartremover.warts.Recursion

class RecursionTest extends FunSuite with ResultAssertions {

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
}
