package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class UnsafeTest extends FunSuite {
  test("can't use `null`, `var`, non-unit statements, Option#get or any2stringadd") {
    val result = WartTestTraverser(Unsafe) {
      var u = {} + "Hello!"
      Some(10).get
      println(null)
    }
    assert(result.errors == List("Scala inserted an any2stringadd call", "Statements must return Unit", "null is disabled", "Option#get is disabled - use Option#fold instead", "var is disabled"))
    assert(result.warnings == List.empty)
  }
}
