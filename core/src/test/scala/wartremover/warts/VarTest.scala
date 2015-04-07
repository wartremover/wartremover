package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Var

class VarTest extends FunSuite {
  test("can't use `var`") {
    val result = WartTestTraverser(Var) {
      var x = 10
      x
    }
    assertResult(List("var is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Var wart obeys ignoreWarts") {
    val result = WartTestTraverser(Var) {
      @ignoreWarts("org.brianmckenna.wartremover.warts.Var")
      var x = 10
      x
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
