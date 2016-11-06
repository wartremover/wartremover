package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Any2StringAdd

class Any2StringAddTest extends FunSuite {
  test("disable any2stringadd") {
    val result = WartTestTraverser(Any2StringAdd) {
      {} + "lol"
    }
    assertResult(List("Scala inserted an any2stringadd call"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("any2stringadd wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Any2StringAdd) {
      @SuppressWarnings(Array("org.wartremover.warts.Any2StringAdd"))
      val foo = {} + "lol"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
