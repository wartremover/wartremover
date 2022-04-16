package org.wartremover
package test

import org.wartremover.warts.ReverseIterator
import org.scalatest.funsuite.AnyFunSuite

class ReverseIteratorTest extends AnyFunSuite with ResultAssertions {
  test("reverse.iterator disabled") {
    val result = WartTestTraverser(ReverseIterator) {
      (1 to 10).reverse.iterator
    }
    assertError(result)("you can use reverseIterator instead of reverse.iterator")
  }

  test("ReverseIterator wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ReverseIterator) {
      @SuppressWarnings(Array("org.wartremover.warts.ReverseIterator"))
      def b = (1 to 10).reverse.iterator
    }
    assertEmpty(result)
  }
}
