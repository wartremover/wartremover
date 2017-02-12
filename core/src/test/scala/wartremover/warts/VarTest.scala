package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Var

class VarTest extends FunSuite with ResultAssertions {
  test("can't use `var`") {
    val result = WartTestTraverser(Var) {
      var x = 10
      x
    }
    assertError(result)("[org.wartremover.warts.Var] var is disabled")
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
