package org.wartremover
package test


import org.wartremover.warts.FinalVal
import org.scalatest.funsuite.AnyFunSuite

class FinalValTest extends AnyFunSuite with ResultAssertions {
  test("final val is disabled") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        final val v = 1
      }
    }
    assertError(result)("final val is disabled - use non-final val or final def or add type ascription")
  }

  test("final val alternatives are enabled") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        val v = 1
        final def v2 = 1
        final val v3: Int = 1
      }
    }
    assertEmpty(result)
  }

  test("FinalVal wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FinalVal) {
      class c {
        @SuppressWarnings(Array("org.wartremover.warts.FinalVal"))
        final val v = 1
      }
    }
    assertEmpty(result)
  }
}
