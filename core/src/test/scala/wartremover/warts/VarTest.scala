package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Var

class VarTest extends FunSuite {
  test("can't use `var`") {
    val result = WartTestTraverser(Var) {
      var x = 10
      x
    }
    assertResult(List("var is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Var wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Var) {
      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      var x = 10
      x
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
