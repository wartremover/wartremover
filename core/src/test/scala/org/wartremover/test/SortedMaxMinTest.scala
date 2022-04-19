package org.wartremover
package test

import org.wartremover.warts.SortedMaxMin
import org.scalatest.funsuite.AnyFunSuite

class SortedMaxMinTest extends AnyFunSuite with ResultAssertions {
  private def seq = 1 to 10
  private def f: Int => String = _.toString
  test("sorted.head disabled") {
    val result = WartTestTraverser(SortedMaxMin) {
      seq.sorted.head
    }
    assertError(result)("You can use min instead of sorted.head")
  }
  test("sorted.last disabled") {
    val result = WartTestTraverser(SortedMaxMin) {
      seq.sorted.last
    }
    assertError(result)("You can use max instead of sorted.last")
  }
  test("sortBy.head disabled") {
    val result = WartTestTraverser(SortedMaxMin) {
      seq.sortBy(f).head
    }
    assertError(result)("You can use minBy instead of sortBy.head")
  }
  test("sortBy.last disabled") {
    val result = WartTestTraverser(SortedMaxMin) {
      seq.sortBy(f).last
    }
    assertError(result)("You can use maxBy instead of sortBy.last")
  }
  test("SortedMaxMin wart obeys SuppressWarnings") {
    val result = WartTestTraverser(SortedMaxMin) {
      @SuppressWarnings(Array("org.wartremover.warts.SortedMaxMin"))
      def foo = List(
        seq.sorted.head,
        seq.sorted.last,
        seq.sortBy(f).head,
        seq.sortBy(f).last
      )
    }
    assertEmpty(result)
  }
}
