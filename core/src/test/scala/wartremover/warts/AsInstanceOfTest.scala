package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.AsInstanceOf

class AsInstanceOfTest extends FunSuite with ResultAssertions {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    assertError(result)("[org.wartremover.warts.AsInstanceOf] asInstanceOf is disabled")
  }
  test("asInstanceOf wart obeys SuppressWarnings") {
    val result = WartTestTraverser(AsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
      val foo = "abc".asInstanceOf[String]
    }
    assertEmpty(result)
  }
}
