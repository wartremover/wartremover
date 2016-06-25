package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Nothing

class NothingTest extends FunSuite {
  test("Nothing can't be inferred") {
    val result = WartTestTraverser(Nothing) {
      val x = ???
      x
    }
    assertResult(List("Inferred type containing Nothing"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Nothing wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Nothing) {
      @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
      val x = ???
      x
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
