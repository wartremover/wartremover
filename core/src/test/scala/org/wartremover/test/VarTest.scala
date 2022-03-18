package org.wartremover
package test


import org.wartremover.warts.Var
import org.scalatest.funsuite.AnyFunSuite

class VarTest extends AnyFunSuite with ResultAssertions {
  test("can't use `var`") {
    val result = WartTestTraverser(Var) {
      var x = 10
      x
    }
    assertError(result)("var is disabled")
  }
  test("Var wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Var) {
      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      var x = 10
      x
    }
    assertEmpty(result)
  }
}
