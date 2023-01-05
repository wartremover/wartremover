package org.wartremover
package test

import org.wartremover.warts.Matchable
import org.scalatest.funsuite.AnyFunSuite
import org.mockito.Mockito

class MatchableTest extends AnyFunSuite with ResultAssertions {
  test("Matchable can't be inferred") {
    val result = WartTestTraverser(Matchable) {
      List("a", true)
    }
    assertError(result)("Inferred type containing Matchable: Matchable")
  }

  test("mockito thenAnswer") {
    val result = WartTestTraverser(Matchable) {
      Mockito.when("a".length).thenAnswer(a => 3)
    }
    assertEmpty(result)
  }

  test("Matchable wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Matchable) {
      @SuppressWarnings(Array("org.wartremover.warts.Matchable"))
      val x = List("a", true)
    }
    assertEmpty(result)
  }
}
