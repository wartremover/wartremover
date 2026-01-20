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
    if (versionNumberString.startsWith("2.12.")) {
      assertError(result)("Inferred type containing Product: Product with Serializable")
    } else if (versionNumberString.startsWith("2.13.")) {
      assertError(result)("Inferred type containing Product: Product with java.io.Serializable")
    } else {
      fail(s"unexpected scala version ${versionNumberString}")
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
