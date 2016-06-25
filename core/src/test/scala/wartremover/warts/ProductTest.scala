package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Product

class ProductTest extends FunSuite {
  test("Product can't be inferred") {
    val result = WartTestTraverser(Product) {
      List((1, 2, 3), (1, 2))
    }
    assertResult(List("Inferred type containing Product"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Product wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Product) {
      @SuppressWarnings(Array("org.wartremover.warts.Product"))
      val foo = List((1, 2, 3), (1, 2))
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
