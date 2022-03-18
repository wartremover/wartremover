package org.wartremover
package test


import org.wartremover.warts.AnyVal
import org.scalatest.funsuite.AnyFunSuite

class AnyValTest extends AnyFunSuite with ResultAssertions {
  test("AnyVal can't be inferred") {
    val result = WartTestTraverser(AnyVal) {
      List(1, true)
    }
    assertError(result)("Inferred type containing AnyVal: AnyVal")
  }

  test("AnyVal wart obeys SuppressWarnings") {
    val result = WartTestTraverser(AnyVal) {
      @SuppressWarnings(Array("org.wartremover.warts.AnyVal"))
      val x = List(1, true)
    }
    assertEmpty(result)
  }
}
