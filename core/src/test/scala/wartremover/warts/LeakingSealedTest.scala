package org.wartremover
package test


import org.wartremover.warts.LeakingSealed
import org.scalatest.funsuite.AnyFunSuite

class LeakingSealedTest extends AnyFunSuite with ResultAssertions {
  test("Descendants of a sealed type must be final or sealed") {
    val result = WartTestTraverser(LeakingSealed) {
      sealed trait t
      class c extends t
    }
    assertError(result)("Descendants of a sealed type must be final or sealed")
  }

  test("Final or sealed descendants of a sealed type are allowed") {
    val result = WartTestTraverser(LeakingSealed) {
      sealed trait t
      final class c extends t
      sealed trait tt extends t
    }
    assertEmpty(result)
  }

  test("LeakingSealed wart obeys SuppressWarnings") {
    val result = WartTestTraverser(LeakingSealed) {
      sealed trait t
      @SuppressWarnings(Array("org.wartremover.warts.LeakingSealed"))
      class c extends t
    }
    assertEmpty(result)
  }
}
