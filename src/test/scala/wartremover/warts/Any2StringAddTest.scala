package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Any2StringAdd

class Any2StringAddTest extends FunSuite {
  test("disable any2stringadd") {
    val result = WartTestTraverser(Any2StringAdd) {
      {} + "lol"
    }
    assert(result.errors == List("Scala inserted an any2stringadd call"))
    assert(result.warnings == List.empty)
  }
}
