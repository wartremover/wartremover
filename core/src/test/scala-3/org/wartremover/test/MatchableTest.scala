package org.wartremover
package test

import org.wartremover.warts.Matchable
import org.scalatest.funsuite.AnyFunSuite

class MatchableTest extends AnyFunSuite with ResultAssertions {
  private def f1: Matchable = 2

  test("Matchable can't be inferred") {
    val result = WartTestTraverser(Matchable) {
      def x1 = f1
    }
    assertError(result)("Inferred type containing Matchable: Matchable")
  }

  test("Matchable wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Matchable) {
      @SuppressWarnings(Array("org.wartremover.warts.Matchable"))
      def x1 = f1
    }
    assertEmpty(result)
  }
}
