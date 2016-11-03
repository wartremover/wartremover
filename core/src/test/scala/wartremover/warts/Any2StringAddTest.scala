package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Any2StringAdd

class Any2StringAddTest extends FunSuite {
  test("Implicit conversion to string is disabled") {
    val result = WartTestTraverser(Any2StringAdd) {
      {} + "lol"
      1 + "lol"
      "lol" + 1
    }
    assertResult(List.fill(3)("Implicit conversion to string is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("other plus usages are allowed") {
    val result = WartTestTraverser(Any2StringAdd) {
      1 + 1
      "a" + "b"
      class C { def +(s: String) = s }
      new C + "a"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("any2stringadd wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Any2StringAdd) {
      @SuppressWarnings(Array("org.wartremover.warts.Any2StringAdd"))
      val foo = {} + "lol"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
