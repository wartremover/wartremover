package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Any

class AnyTest extends FunSuite {
  test("Any can't be inferred") {
    val result = WartTestTraverser(Any) {
      val x = readf1("{0}")
      x
    }
    assert(result.errors == List("Inferred type containing Any from assignment"))
    assert(result.warnings == List.empty)
  }
}
