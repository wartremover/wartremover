package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class UnsafeTest extends FunSuite {
  test("can't use `null`, `var`, non-unit statements or any2stringadd") {
    val result = WartTestTraverser(Unsafe) {
      var u = {} + "Hello!"
      10
      println(null)
    }
    assert(result.errors == List("Statements must return Unit", "var is disabled", "null is disabled", "Scala inserted an any2stringadd call"))
    assert(result.warnings == List.empty)
  }
}
