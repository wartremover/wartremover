package org.wartremover
package test

import org.wartremover.warts.IsInstanceOf
import org.scalatest.funsuite.AnyFunSuite

class IsInstanceOfTest extends AnyFunSuite with ResultAssertions {
  test("isInstanceOf is disabled") {
    val result = WartTestTraverser(IsInstanceOf) {
      "abc".isInstanceOf[String]
    }
    assertError(result)("isInstanceOf is disabled")
  }
  test("issue 152") {
    // https://github.com/wartremover/wartremover/issues/152
    val result = WartTestTraverser(IsInstanceOf) {
      (Option(1), 1) match {
        case (x @ Some(_), _) => x
        case _ => None
      }
    }
    assertEmpty(result)
  }
  test("isInstanceOf wart obeys SuppressWarnings") {
    val result = WartTestTraverser(IsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf"))
      val foo = "abc".isInstanceOf[String]
    }
    assertEmpty(result)
  }
  test("isInstanceOf should not check macro expansions") {
    val result = WartTestTraverser(IsInstanceOf) {
      IsInstanceOfTestMacros.is[Object, String]
    }
    assertEmpty(result)
  }
}
