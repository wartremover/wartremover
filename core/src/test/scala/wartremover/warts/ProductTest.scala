package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.{Product, ProductOps}

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
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Product"))
      val foo = List((1, 2, 3), (1, 2))
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use Product#canEqual") {
    val result = WartTestTraverser(ProductOps) {
      println(Option(1).canEqual("foo"))
    }
    assertResult(List("Product#canEqual is disabled - consider using type classes for type-safe equality instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use Product#productArity") {
    val result = WartTestTraverser(ProductOps) {
      println(Option(1).productArity)
    }
    assertResult(List("Product#productArity is disabled - it is not a very helpful abstraction"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use Product#productElement") {
    val result = WartTestTraverser(ProductOps) {
      println(Option(1).productElement(-1))
    }
    assertResult(List("Product#productElement is disabled - it is unsafe and its return type of Any is not a very helpful abstraction"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use Product#productIterator") {
    val result = WartTestTraverser(ProductOps) {
      println(Option(1).productIterator)
    }
    assertResult(List("Product#produceIterator is disabled - Iterator[Any] is not a very helpful abstraction"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use Product#productPrefix") {
    val result = WartTestTraverser(ProductOps) {
      println(Option(1).productPrefix)
    }
    assertResult(List("Product#productPrefix is disabled - it is not a very helpful abstraction"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
