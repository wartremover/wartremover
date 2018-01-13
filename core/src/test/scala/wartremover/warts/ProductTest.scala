package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Product

class ProductTest extends FunSuite with ResultAssertions {
  test("Product can't be inferred") {
    val result = WartTestTraverser(Product) {
      List((1, 2, 3), (1, 2))
    }
    assertError(result)("Inferred type containing Product: Product with Serializable")
  }

  test("Product wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Product) {
      @SuppressWarnings(Array("org.wartremover.warts.Product"))
      val foo = List((1, 2, 3), (1, 2))
    }
    assertEmpty(result)
  }
}
