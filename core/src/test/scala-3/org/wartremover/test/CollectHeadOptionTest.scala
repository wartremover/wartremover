package org.wartremover
package test

import org.wartremover.warts.CollectHeadOption
import org.scalatest.funsuite.AnyFunSuite

class CollectHeadOptionTest extends AnyFunSuite with ResultAssertions {
  test("collect.headOption disabled") {
    val result = WartTestTraverser(CollectHeadOption) {
      List(1, 2, 3).collect { case n if n % 2 == 0 => n }.headOption
    }
    assertError(result)("you can use collectFirst instead of collect.headOption")
  }

  test("CollectHeadOption wart obeys SuppressWarnings") {
    val result = WartTestTraverser(CollectHeadOption) {
      @SuppressWarnings(Array("org.wartremover.warts.CollectHeadOption"))
      def x = List(1, 2, 3).collect { case n if n % 2 == 0 => n }.headOption
    }
    assertEmpty(result)
  }
}
