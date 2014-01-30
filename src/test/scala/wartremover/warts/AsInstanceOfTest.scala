package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.AsInstanceOf

class AsInstanceOfTest extends FunSuite {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    expectResult(List("asInstanceOf is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
