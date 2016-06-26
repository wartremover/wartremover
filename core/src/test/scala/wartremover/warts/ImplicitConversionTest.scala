package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ImplicitConversion

class ImplicitConversionTest extends FunSuite {
  test("Implicit conversion is disabled") {
    val result = WartTestTraverser(ImplicitConversion) {
      class c {
        implicit def int2Array(i: Int): Array[String] = Array.fill(i)("?")
      }
    }
    assertResult(List("Implicit conversion is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Non-public implicit conversion is enabled") {
    val result = WartTestTraverser(ImplicitConversion) {
      class c {
        protected implicit def int2Array(i: Int): Array[String] = Array.fill(i)("?")
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Implicit evidence constructor is enabled") {
    val result = WartTestTraverser(ImplicitConversion) {
      implicit def ordering[A]: Ordering[A] = ???
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("ImplicitConversion wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ImplicitConversion) {
      class c {
        @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
        implicit def int2Array(i: Int): Array[String] = Array.fill(i)("?")
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
