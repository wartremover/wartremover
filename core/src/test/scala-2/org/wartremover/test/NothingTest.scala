package org.wartremover
package test

import org.wartremover.warts.Nothing
import org.scalatest.funsuite.AnyFunSuite

// TODO Scala 3
class NothingTest extends AnyFunSuite with ResultAssertions {
  test("Nothing can't be inferred") {
    val result = WartTestTraverser(Nothing) {
      val x = ???
      x
    }
    assertError(result)("Inferred type containing Nothing: Nothing")
  }
  test("Nothing wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Nothing) {
      @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
      val x = ???
      x
    }
    assertEmpty(result)
  }
}
