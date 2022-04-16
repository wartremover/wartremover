package org.wartremover
package test

import org.wartremover.warts.FilterEmpty
import org.scalatest.funsuite.AnyFunSuite

class FilterEmptyTest extends AnyFunSuite with ResultAssertions {
  private val seq = 1 to 10
  test("filter.isEmpty disabled") {
    val result = WartTestTraverser(FilterEmpty) {
      seq.filter(_ < 3).isEmpty
    }
    assertError(result)("you can use exists instead of filter.isEmpty")
  }
  test("filter.nonEmpty disabled") {
    val result = WartTestTraverser(FilterEmpty) {
      seq.filter(_ < 4).nonEmpty
    }
    assertError(result)("you can use exists instead of filter.nonEmpty")
  }
  test("filterNot.isEmpty disabled") {
    val result = WartTestTraverser(FilterEmpty) {
      seq.filterNot(_ < 5).isEmpty
    }
    assertError(result)("you can use forall instead of filterNot.isEmpty")
  }
  test("filterNot.nonEmpty disabled") {
    val result = WartTestTraverser(FilterEmpty) {
      seq.filterNot(_ < 6).nonEmpty
    }
    assertError(result)("you can use forall instead of filterNot.nonEmpty")
  }

  test("FilterEmpty wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FilterEmpty) {
      @SuppressWarnings(Array("org.wartremover.warts.FilterEmpty"))
      class A {
        def x1 = seq.filter(_ > 1).isEmpty
        def x2 = seq.filter(_ > 2).nonEmpty
        def x3 = seq.filterNot(_ > 3).isEmpty
        def x4 = seq.filterNot(_ > 4).nonEmpty
      }
    }
    assertEmpty(result)
  }
}
