package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Product

class ProductTest extends FunSuite {
  test("Product can't be inferred") {
    val result = WartTestTraverser(Product) {
      List((1, 2, 3), (1, 2))
    }
    assert(result.errors == List("Inferred type containing Product"))
    assert(result.warnings == List.empty)
  }
}
