package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.AsInstanceOf

class AsInstanceOfTest extends FunSuite {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    assertResult(List("asInstanceOf is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("asInstanceOf wart obeys ignoreWarts") {
    val result = WartTestTraverser(AsInstanceOf) {
      @ignoreWarts("org.brianmckenna.wartremover.warts.AsInstanceOf")
      val foo = "abc".asInstanceOf[String]
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
