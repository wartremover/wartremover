package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Any2StringAdd

class Any2StringAddTest extends FunSuite with ResultAssertions {
  test("disable any2stringadd") {
    val result = WartTestTraverser(Any2StringAdd) {
      {} + "lol"
    }
    assertError(result)(s"[org.wartremover.warts.Any2StringAdd] Scala inserted an any2stringadd call")
  }
  test("any2stringadd wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Any2StringAdd) {
      @SuppressWarnings(Array("org.wartremover.warts.Any2StringAdd"))
      val foo = {} + "lol"
    }
    assertEmpty(result)
  }
}
