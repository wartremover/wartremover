package org.wartremover
package test


import org.wartremover.warts.ArrayEquals
import org.scalatest.funsuite.AnyFunSuite

class ArrayEqualsTest extends AnyFunSuite with ResultAssertions {
  test("Array == is disabled") {
    val result = WartTestTraverser(ArrayEquals) {
      Array(1) == Array(1)
    }
    assertError(result)("== is disabled, use sameElements instead")
  }

  test("Iterator == is disabled") {
    val result = WartTestTraverser(ArrayEquals) {
      Iterator(1) == Iterator(1)
    }
    assertError(result)("== is disabled, use sameElements instead")
  }

  test("`Array == null` is allowed") {
    val result = WartTestTraverser(ArrayEquals) {
      Array(1) == null
    }
    assertEmpty(result)
  }

  test("Collections == is allowed") {
    val result = WartTestTraverser(ArrayEquals) {
      List(1) == List(1)
    }
    assertEmpty(result)
  }

  test("ArrayEquals wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ArrayEquals) {
      @SuppressWarnings(Array("org.wartremover.warts.ArrayEquals"))
      def f = Array(1) == Array(1)
    }
    assertEmpty(result)
  }
}
