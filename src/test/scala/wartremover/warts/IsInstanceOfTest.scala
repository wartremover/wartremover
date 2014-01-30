package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.IsInstanceOf

class IsInstanceOfTest extends FunSuite {
  test("isInstanceOf is disabled") {
    val result = WartTestTraverser(IsInstanceOf) {
      "abc".isInstanceOf[String]
    }
    assert(result.errors == List("isInstanceOf is disabled"))
    assert(result.warnings == List.empty)
  }
}
