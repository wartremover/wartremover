package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Any2StringAdd

class Any2StringAddTest extends FunSuite {
  test("disable any2stringadd") {
    val result = WartTestTraverser(Any2StringAdd) {
      {} + "lol"
    }
    assertResult(List("Scala inserted an any2stringadd call"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("any2stringadd wart obeys ignoreWarts") {
    val result = WartTestTraverser(Any2StringAdd) {
      @ignoreWarts("org.brianmckenna.wartremover.warts.Any2StringAdd")
      val foo = {} + "lol"
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
