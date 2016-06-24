package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Unsafe

class UnsafeTest extends FunSuite {
  test("can't use `null`, `var`, non-unit statements, Option#get, LeftProjection#get, RightProjection#get, or any2stringadd") {
    val result = WartTestTraverser(Unsafe) {
      val x = List(1, true, "three")
      var u = {} + "Hello!"
      Some(10).get
      println(Left(42).left.get)
      println(Left(42).right.get)
      println(Right(42).left.get)
      println(Right(42).right.get)
      println(null)
    }
    assertResult(
      Set("Inferred type containing Any",
           "Inferred type containing Any",
           "Scala inserted an any2stringadd call",
           "LeftProjection#get is disabled - use LeftProjection#toOption instead",
           "RightProjection#get is disabled - use RightProjection#toOption instead",
           "LeftProjection#get is disabled - use LeftProjection#toOption instead",
           "RightProjection#get is disabled - use RightProjection#toOption instead",
           "Statements must return Unit",
           "null is disabled",
           "Option#get is disabled - use Option#fold instead",
           "var is disabled"), "result.errors")(result.errors.toSet)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
