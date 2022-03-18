package org.wartremover
package test

import scala.util.Properties.versionNumberString


import org.wartremover.warts.Product
import org.scalatest.funsuite.AnyFunSuite

class ProductTest extends AnyFunSuite with ResultAssertions {
  test("Product can't be inferred") {
    val result = WartTestTraverser(Product) {
      List((1, 2, 3), (1, 2))
    }
    if (versionNumberString.matches("2\\.1[012].*")) {
      assertError(result)("Inferred type containing Product: Product with Serializable")
    } else {
      assertError(result)("Inferred type containing Product: Product with java.io.Serializable")
    }
  }
  test("Product wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Product) {
      @SuppressWarnings(Array("org.wartremover.warts.Product"))
      val foo = List((1, 2, 3), (1, 2))
    }
    assertEmpty(result)
  }
}
