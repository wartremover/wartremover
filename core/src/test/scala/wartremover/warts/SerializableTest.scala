package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Serializable

class SerializableTest extends FunSuite {
  test("Serializable can't be inferred") {
    val result = WartTestTraverser(Serializable) {
      List((1, 2, 3), (1, 2))
    }
    assertResult(List("Inferred type containing Serializable"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Serializable wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Serializable) {
      @SuppressWarnings(Array("org.wartremover.warts.Serializable"))
      val foo = List((1, 2, 3), (1, 2))
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
