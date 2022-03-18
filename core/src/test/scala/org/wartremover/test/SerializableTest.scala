package org.wartremover
package test

import scala.util.Properties.versionNumberString


import org.wartremover.warts.Serializable
import org.scalatest.funsuite.AnyFunSuite

class SerializableTest extends AnyFunSuite with ResultAssertions {
  test("Serializable can't be inferred") {
    val result = WartTestTraverser(Serializable) {
      List((1, 2, 3), (1, 2))
    }
    if (versionNumberString.matches("2\\.1[012].*")) {
      assertError(result)("Inferred type containing Serializable: Product with Serializable")
    } else {
      assertError(result)("Inferred type containing Serializable: Product with java.io.Serializable")
    }
  }
  test("Serializable wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Serializable) {
      @SuppressWarnings(Array("org.wartremover.warts.Serializable"))
      val foo = List((1, 2, 3), (1, 2))
    }
    assertEmpty(result)
  }
}
