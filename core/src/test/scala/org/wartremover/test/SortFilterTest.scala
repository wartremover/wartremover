package org.wartremover
package test

import org.wartremover.warts.SortFilter
import org.scalatest.funsuite.AnyFunSuite

class SortFilterTest extends AnyFunSuite with ResultAssertions {
  test("sorted.filter disabled") {
    val result = WartTestTraverser(SortFilter) {
      (1 to 10).sorted.filter(_ => false)
    }
    assertError(result)("Change order of `sorted` and `filter`")
  }

  test("sortBy.filter disabled") {
    val result = WartTestTraverser(SortFilter) {
      (1 to 10).sortBy(-_).filter(_ % 3 != 0)
    }
    assertError(result)("Change order of `sortBy` and `filter`")
  }

  test("sortWith.filter disabled") {
    val result = WartTestTraverser(SortFilter) {
      (1 to 10).sortWith(_ > _).filter(_ % 2 == 0)
    }
    assertError(result)("Change order of `sortWith` and `filter`")
  }

  test("SortFilter wart obeys SuppressWarnings") {
    val result = WartTestTraverser(SortFilter) {
      @SuppressWarnings(Array("org.wartremover.warts.SortFilter"))
      class X {
        def a1 = (1 to 10).sorted.filter(_ => false)
        def a2 = (1 to 10).sortBy(-_).filter(_ % 3 != 0)
        def a3 = (1 to 10).sortWith(_ > _).filter(_ % 2 == 0)
      }
    }
    assertEmpty(result)
  }
}
