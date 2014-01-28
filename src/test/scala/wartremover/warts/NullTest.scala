package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Null

class NullTest extends FunSuite {
  test("can't use `null`") {
    val result = WartTestTraverser(Null) {
      println(null)
    }
    expectResult(List("null is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use null in patterns") {
    val result = WartTestTraverser(Null) {
      val (a, b) = (1, null)
      println(a)
    }
    expectResult(List("null is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use null inside of Map#partition") {
    val result = WartTestTraverser(Null) {
      Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
    }
    expectResult(List("null is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
