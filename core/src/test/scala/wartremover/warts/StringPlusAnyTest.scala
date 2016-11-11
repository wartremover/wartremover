package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.StringPlusAny

class StringPlusAnyTest extends FunSuite with ResultAssertions {
  test("Implicit conversion to string is disabled") {
    val result = WartTestTraverser(StringPlusAny) {
      {} + "lol"
      1 + "lol"
      "lol" + 1
      "" + (if (true) 5 else "")
    }
    assertErrors(result)("Implicit conversion to string is disabled", 4)
  }

  test("Non-string + usage is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      1 + 1
    }
    assertEmpty(result)
  }

  test("string literal concatenation is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      "a" + "b"
    }
    assertEmpty(result)
  }

  test("concatenating strings with if statements is allowed.") {
    val result = WartTestTraverser(StringPlusAny) {
      "" + (if (true) "" else "")
      (if (true) "" else "") + ""
    }
    assertEmpty(result)
  }

  test("custom-defined + is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      class C { def +(s: String) = s }
      new C + "a"
    }
    assertEmpty(result)
  }

  test("StringPlusAny wart obeys SuppressWarnings") {
    val result = WartTestTraverser(StringPlusAny) {
      @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
      val foo = {} + "lol"
    }
    assertEmpty(result)
  }
}
