package org.wartremover
package test

import org.wartremover.warts.FilterHeadOption
import org.scalatest.funsuite.AnyFunSuite

class FilterHeadOptionTest extends AnyFunSuite with ResultAssertions {
  test("filter.haddOption disabled") {
    val result = WartTestTraverser(FilterHeadOption) {
      List(1, 2, 3).filter(_ < 3).headOption
    }
    assertError(result)("you can use find instead of filter.headOption")
  }

  test("FilterHeadOption wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FilterHeadOption) {
      @SuppressWarnings(Array("org.wartremover.warts.FilterHeadOption"))
      def x = List(1, 2, 3).filter(_ < 3).headOption
    }
    assertEmpty(result)
  }
}
