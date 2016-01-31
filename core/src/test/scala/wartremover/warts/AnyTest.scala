package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Any

class AnyTest extends FunSuite {
  test("Any can't be inferred") {
    val result = WartTestTraverser(Any) {
      val x = readf1("{0}")
      x
    }
    assertResult(List("Inferred type containing Any"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Any wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Any) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
      val x = readf1("{0}")
      x
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Any can be inferred if it must be") {
    val result = WartTestTraverser(Any) {
      def f[A >: Any] = 42
      f
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
