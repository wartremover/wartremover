package org.wartremover.test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.TripleQuestionMark

class TripleQuestionMarkTest extends AnyFunSuite with ResultAssertions {
  test("??? is disabled") {
    val result = WartTestTraverser(TripleQuestionMark) {
      def foo(n: Int): Int = ???
    }
    assertError(result)("??? is disabled")
  }

  test("doesn't detect other `???` methods") {
    val result = WartTestTraverser(TripleQuestionMark) {
      case class A(`???`: Int)
      println(A(1).`???`)
    }
    assertEmpty(result)
  }

  test("Throw wart obeys SuppressWarnings") {
    val result = WartTestTraverser(TripleQuestionMark) {
      @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
      def foo(n: Int): Int = ???
    }
    assertEmpty(result)
  }
}
