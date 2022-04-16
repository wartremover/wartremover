package org.wartremover
package test

import org.wartremover.warts.ReverseTakeReverse
import org.scalatest.funsuite.AnyFunSuite

class ReverseTakeReverseTest extends AnyFunSuite with ResultAssertions {
  test("reverse.take.reverse disabled") {
    val result = WartTestTraverser(ReverseTakeReverse) {
      (1 to 10).reverse.take(3).reverse
    }
    assertError(result)("you can use takeRight instead of reverse.take.reverse")
  }

  test("ReverseTakeReverse wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ReverseTakeReverse) {
      @SuppressWarnings(Array("org.wartremover.warts.ReverseTakeReverse"))
      def z = (1 to 10).reverse.take(3).reverse
    }
    assertEmpty(result)
  }
}
