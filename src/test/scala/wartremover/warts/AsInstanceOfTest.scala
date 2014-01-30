package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.AsInstanceOf

class AsInstanceOfTest extends FunSuite {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    assert(result.errors == List("asInstanceOf is disabled"))
    assert(result.warnings == List.empty)
  }
}
