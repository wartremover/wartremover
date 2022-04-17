package org.wartremover
package test

import org.wartremover.warts.CollectHeadOption
import org.scalatest.funsuite.AnyFunSuite

class CollectHeadOptionTest extends AnyFunSuite with ResultAssertions {
  test("collect.headOption disabled") {
    def pf: PartialFunction[Int, String] = { case a => a.toString }
    val result = WartTestTraverser(CollectHeadOption) {
      List(1, 2, 3).collect { case n if n % 2 == 0 => n }.headOption
      List(1, 2, 3).collect(pf).headOption
    }
    assertErrors(result)("you can use collectFirst instead of collect.headOption", 2)
  }

  test("CollectHeadOption wart obeys SuppressWarnings") {
    val result = WartTestTraverser(CollectHeadOption) {
      @SuppressWarnings(Array("org.wartremover.warts.CollectHeadOption"))
      def x = List(1, 2, 3).collect { case n if n % 2 == 0 => n }.headOption
    }
    assertEmpty(result)
  }
}
