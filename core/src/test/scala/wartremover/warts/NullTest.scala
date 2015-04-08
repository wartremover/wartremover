package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Null

class NullTest extends FunSuite {
  test("can't use `null`") {
    val result = WartTestTraverser(Null) {
      println(null)
    }
    assertResult(List("null is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use null in patterns") {
    val result = WartTestTraverser(Null) {
      val (a, b) = (1, null)
      println(a)
    }
    assertResult(List("null is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use null inside of Map#partition") {
    val result = WartTestTraverser(Null) {
      Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
    }
    assertResult(List("null is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Null wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Null) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Null"))
      val foo = {
        println(null)
        val (a, b) = (1, null)
        println(a)
        Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
