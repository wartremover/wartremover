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
    assert(result.errors == List("var is disabled"))
    assert(result.warnings == List.empty)
  }
}
