package org.wartremover
package test

import org.wartremover.warts.FilterSize
import org.scalatest.funsuite.AnyFunSuite
import scala.collection.mutable.ArrayBuffer

class FilterSizeTest extends AnyFunSuite with ResultAssertions {
  test("filter.size disabled") {
    val result1 = WartTestTraverser(FilterSize) {
      Iterable(1, 2, 3).filter(_ < 3).size
    }
    assertError(result1)("you can use count instead of filter.size")
    val result2 = WartTestTraverser(FilterSize) {
      ArrayBuffer(1, 2, 3).filter(_ < 3).length
    }
    assertError(result2)("you can use count instead of filter.length")
  }

  test("FilterSize wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FilterSize) {
      @SuppressWarnings(Array("org.wartremover.warts.FilterSize"))
      class Y {
        def x1 = Iterable(1, 2, 3).filter(_ < 3).size
        def x2 = ArrayBuffer(1, 2, 3).filter(_ < 3).length
      }
    }
    assertEmpty(result)
  }
}
