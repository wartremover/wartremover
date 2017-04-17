package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ArrayEquals

class ArrayEqualsTest extends FunSuite with ResultAssertions {
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
