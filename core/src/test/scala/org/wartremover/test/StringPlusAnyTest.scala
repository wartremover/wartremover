package org.wartremover
package test

import org.wartremover.warts.StringPlusAny
import org.scalatest.funsuite.AnyFunSuite

class StringPlusAnyTest extends AnyFunSuite with ResultAssertions {
  test("string interpolation") {
    val result = WartTestTraverser(StringPlusAny) {
      List(
        s"a ${2} b",
        s"${false} c",
        s"d ${1.5}"
      )
    }
    assertEmpty(result)
  }

  test("Implicit conversion to string is disabled") {
    val result = WartTestTraverser(StringPlusAny) {
      "lol" + 1
      "" + (if (true) 5 else "")
    }
    assertErrors(result)("Implicit conversion to string is disabled", 2)
  }

  test("Primitive conversion to string is disabled") {
    val result = WartTestTraverser(StringPlusAny) {
      1 + "lol"
    }
    assertError(result)("Implicit conversion to string is disabled")
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

  test("inserting into a Set is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      Set("") + ""
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

  test("Concatenation of a string with a block containing an if-statement is allowed.") {
    val result = WartTestTraverser(StringPlusAny) {
      "" + { val x = ""; if (true) x else x }
    }
    assertEmpty(result)
  }

  test("Adding float variables to an int value is allowed.") {
    val result = WartTestTraverser(StringPlusAny) {
      val a: Float = 1
      0 + a
    }
    assertEmpty(result)
  }

  test("StringPlusAny wart obeys SuppressWarnings") {
    val result = WartTestTraverser(StringPlusAny) {
      @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
      class A {
        def a1 = "lol" + 1
        def a2 = "" + (if (true) 5 else "")
        def a3 = 1 + "x"
      }
    }
    assertEmpty(result)
  }

  test("adding Float values is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      1f + 1f
    }
    assertEmpty(result)
  }

  test("adding with StringOps is allowed") {
    val result = WartTestTraverser(StringPlusAny) {
      "" + ("" padTo (1, ' '))
    }
    assertEmpty(result)
  }
}
