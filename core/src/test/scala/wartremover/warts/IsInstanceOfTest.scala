package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.IsInstanceOf

class IsInstanceOfTest extends FunSuite {
  test("isInstanceOf is disabled") {
    val result = WartTestTraverser(IsInstanceOf) {
      "abc".isInstanceOf[String]
    }
    assertResult(List("isInstanceOf is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("isInstanceOf wart obeys SuppressWarnings") {
    val result = WartTestTraverser(IsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf"))
      val foo = "abc".isInstanceOf[String]
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
