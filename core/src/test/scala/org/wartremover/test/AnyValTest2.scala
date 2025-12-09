package org.wartremover
package test

import org.wartremover.warts.AnyVal
import org.scalatest.funsuite.AnyFunSuite

class AnyValTest2 extends AnyFunSuite with ResultAssertions {
  private def f1: AnyVal = 7

  test("AnyVal can't be inferred") {
    val result = WartTestTraverser(AnyVal) {
      def x1 = f1
    }
    assertError(result)("Inferred type containing AnyVal: AnyVal")
  }

  test("AnyVal wart obeys SuppressWarnings") {
    val result = WartTestTraverser(AnyVal) {
      @SuppressWarnings(Array("org.wartremover.warts.AnyVal"))
      def x1 = f1
    }
    assertEmpty(result)
  }
}
