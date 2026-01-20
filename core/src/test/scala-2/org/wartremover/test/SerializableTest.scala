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
    if (versionNumberString.startsWith("2.12.")) {
      assertError(result)("Inferred type containing Serializable: Product with Serializable")
    } else if (versionNumberString.startsWith("2.13.")) {
      assertError(result)("Inferred type containing Serializable: Product with java.io.Serializable")
    } else {
      fail(s"unexpected scala version ${versionNumberString}")
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
