package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Product

class ProductTest extends FunSuite {
  test("Product can't be inferred") {
    val result = WartTestTraverser(Product) {
      List((1, 2, 3), (1, 2))
    }
    expectResult(List("Inferred type containing Product"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
