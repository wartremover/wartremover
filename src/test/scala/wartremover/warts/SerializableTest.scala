package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Serializable

class SerializableTest extends FunSuite {
  test("Serializable can't be inferred") {
    val result = WartTestTraverser(Serializable) {
      List((1, 2, 3), (1, 2))
    }
    assert(result.errors == List("Inferred type containing Serializable"))
    assert(result.warnings == List.empty)
  }
}
