package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.FinalVal

class FinalValTest extends FunSuite {
  test("final val is disabled") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        final val v = 1
      }
    }
    assertResult(List("final val is disabled - use non-final val or final def instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("non-final vals are enabled") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        val v = 1
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("FinalVal wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        @SuppressWarnings(Array("org.wartremover.warts.FinalVal"))
        final val v = 1
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
