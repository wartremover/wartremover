package org.wartremover
package test

import org.wartremover.warts.ReverseFind
import org.scalatest.funsuite.AnyFunSuite

class ReverseFindTest extends AnyFunSuite with ResultAssertions {
  test("reverse.find disabled") {
    val result = WartTestTraverser(ReverseFind) {
      Vector(5, 4, 3, 2, 1).reverse.find(_ > 2)
    }
    assertError(result)("you can use findLast instead of reverse.find")
  }

  test("ReverseFind wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ReverseFind) {
      @SuppressWarnings(Array("org.wartremover.warts.ReverseFind"))
      def a = Vector(5, 4, 3, 2, 1).reverse.find(_ > 2)
    }
    assertEmpty(result)
  }
}
