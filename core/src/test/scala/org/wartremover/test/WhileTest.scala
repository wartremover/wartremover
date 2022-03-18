package org.wartremover
package test

import org.wartremover.warts.While
import org.scalatest.funsuite.AnyFunSuite
import scala.annotation.nowarn

class WhileTest extends AnyFunSuite with ResultAssertions {
  test("while is disabled") {
    val result = WartTestTraverser(While) {
      while (true) {
        println()
      }
    }
    assertError(result)("while is disabled")
  }

  test("do while is disabled") {
    @nowarn("msg=no longer supported")
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
        while (true) {}
      }
    }
    assertEmpty(result)
  }
}
