package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.AsInstanceOf

class AsInstanceOfTest extends FunSuite with ResultAssertions {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    assertError(result)("asInstanceOf is disabled")
  }
  test("asInstanceOf is disabled in anonymous PartialFunction") {
    val result = WartTestTraverser(AsInstanceOf) {
      List(1).collect { case x if { x.asInstanceOf[Integer]; true } => x }
    }
    assertError(result)("asInstanceOf is disabled")
  }
  test("asInstanceOf wart obeys SuppressWarnings") {
    val result = WartTestTraverser(AsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
      val foo = "abc".asInstanceOf[String]
    }
    assertEmpty(result)
  }
}
