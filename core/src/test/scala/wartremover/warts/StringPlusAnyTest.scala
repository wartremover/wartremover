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
      "" + (if (true) 5 else "")
    }
    assertResult(List.fill(4)("Implicit conversion to string is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Non-string + usage is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      1 + 1
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("string literal concatenation is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      "a" + "b"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("concatenating strings with if statements is allowed.") {
    val result = WartTestTraverser(StringPlusAny) {
      "" + (if (true) "" else "")
      (if (true) "" else "") + ""
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("custom-defined + is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
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
