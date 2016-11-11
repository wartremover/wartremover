package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.While

class WhileTest extends FunSuite with ResultAssertions {
  test("while is disabled") {
    val result = WartTestTraverser(While) {
      while (true) {
        println()
      }
    }
    assertError(result)("while is disabled")
  }

  test("do while is disabled") {
    val result = WartTestTraverser(While) {
      do {
        println()
      } while (true)
    }
    assertError(result)("while is disabled")
  }

  test("while wart obeys SuppressWarnings") {
    val result = WartTestTraverser(While) {
      @SuppressWarnings(Array("org.wartremover.warts.While"))
      def f() = {
        while (true) {
        }
      }
    }
    assertEmpty(result)
  }
}
