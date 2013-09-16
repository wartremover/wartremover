package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Null

class NullTest extends FunSuite {
  test("can't use `null`") {
    val result = WartTestTraverser(Null) {
      println(null)
    }
    assert(result.errors == List("null is disabled"))
    assert(result.warnings == List.empty)
  }
}
