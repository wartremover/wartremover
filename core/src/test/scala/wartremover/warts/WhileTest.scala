package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.While

class WhileTest extends FunSuite {
  test("while is disabled") {
    val result = WartTestTraverser(While) {
      while (true) {
        println()
      }
    }
    assertResult(List("while is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("do while is disabled") {
    val result = WartTestTraverser(While) {
      do {
        println()
      } while (true)
    }
    assertResult(List("while is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("while wart obeys SuppressWarnings") {
    val result = WartTestTraverser(While) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.While"))
      def f() = {
        while (true) {
        }
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
