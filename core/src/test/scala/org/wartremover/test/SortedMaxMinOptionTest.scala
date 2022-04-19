package org.wartremover
package test

import org.wartremover.warts.SortedMaxMinOption
import org.scalatest.funsuite.AnyFunSuite

class SortedMaxMinOptionTest extends AnyFunSuite with ResultAssertions {
  private def seq = 1 to 10
  private def f: Int => String = _.toString
  test("sorted.headOption disabled") {
    val result = WartTestTraverser(SortedMaxMinOption) {
      seq.sorted.headOption
    }
    assertError(result)("You can use minOption instead of sorted.headOption")
  }
  test("sorted.lastOption disabled") {
    val result = WartTestTraverser(SortedMaxMinOption) {
      seq.sorted.lastOption
    }
    assertError(result)("You can use maxOption instead of sorted.lastOption")
  }
  test("sortBy.headOption disabled") {
    val result = WartTestTraverser(SortedMaxMinOption) {
      seq.sortBy(f).headOption
    }
    assertError(result)("You can use minByOption instead of sortBy.headOption")
  }
  test("sortBy.lastOption disabled") {
    val result = WartTestTraverser(SortedMaxMinOption) {
      seq.sortBy(f).lastOption
    }
    assertError(result)("You can use maxByOption instead of sortBy.lastOption")
  }
  test("SortedMaxMinOption wart obeys SuppressWarnings") {
    val result = WartTestTraverser(SortedMaxMinOption) {
      @SuppressWarnings(Array("org.wartremover.warts.SortedMaxMinOption"))
      def foo = List(
        seq.sorted.headOption,
        seq.sorted.lastOption,
        seq.sortBy(f).headOption,
        seq.sortBy(f).lastOption
      )
    }
    assertEmpty(result)
  }
}
