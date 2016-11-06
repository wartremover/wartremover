package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.StringPlusAny

class StringPlusAnyTest extends FunSuite {
  test("Implicit conversion to string is disabled") {
    val result = WartTestTraverser(StringPlusAny) {
      {} + "lol"
      1 + "lol"
      "lol" + 1
    }
    assertResult(List.fill(3)("Implicit conversion to string is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("other plus usages are allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      1 + 1
      "a" + "b"
      class C { def +(s: String) = s }
      new C + "a"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("StringPlusAny wart obeys SuppressWarnings") {
    val result = WartTestTraverser(StringPlusAny) {
      @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
      val foo = {} + "lol"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
