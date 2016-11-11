package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Any

class AnyTest extends FunSuite with ResultAssertions {
  test("Any can't be inferred") {
    val result = WartTestTraverser(Any) {
      val x = readf1("{0}")
      x
    }
    assertError(result)("Inferred type containing Any")
  }
  test("Any wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Any) {
      @SuppressWarnings(Array("org.wartremover.warts.Any"))
      val x = readf1("{0}")
      x
    }
    assertEmpty(result)
  }
}
