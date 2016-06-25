package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.LeakingSealed

class LeakingSealedTest extends FunSuite {
  test("Descendants of a sealed type must be final or sealed") {
    val result = WartTestTraverser(LeakingSealed) {
      sealed trait t
      class c extends t
    }
    assertResult(List("Descendants of a sealed type must be final or sealed"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Final or sealed descendants of a sealed type are allowed") {
    val result = WartTestTraverser(LeakingSealed) {
      sealed trait t
      final class c extends t
      sealed trait tt extends t
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("LeakingSealed wart obeys SuppressWarnings") {
    val result = WartTestTraverser(LeakingSealed) {
      sealed trait t
      @SuppressWarnings(Array("org.wartremover.warts.LeakingSealed"))
      class c extends t
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
