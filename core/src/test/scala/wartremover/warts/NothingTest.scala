package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Nothing

class NothingTest extends FunSuite {
  test("Nothing can't be inferred") {
    val result = WartTestTraverser(Nothing) {
      val x = ???
      x
    }
    assertResult(List("Inferred type containing Nothing"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Nothing wart obeys ignoreWarts") {
    val result = WartTestTraverser(Nothing) {
      @ignoreWarts("org.brianmckenna.wartremover.warts.Nothing")
      val x = ???
      x
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
