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
      Set("[org.wartremover.warts.Unsafe] Inferred type containing Any",
           "[org.wartremover.warts.Unsafe] Inferred type containing Any",
           "[org.wartremover.warts.Unsafe] Scala inserted an any2stringadd call",
           "[org.wartremover.warts.Unsafe] LeftProjection#get is disabled - use LeftProjection#toOption instead",
           "[org.wartremover.warts.Unsafe] RightProjection#get is disabled - use RightProjection#toOption instead",
           "[org.wartremover.warts.Unsafe] LeftProjection#get is disabled - use LeftProjection#toOption instead",
           "[org.wartremover.warts.Unsafe] RightProjection#get is disabled - use RightProjection#toOption instead",
           "[org.wartremover.warts.Unsafe] Statements must return Unit",
           "[org.wartremover.warts.Unsafe] null is disabled",
           "[org.wartremover.warts.Unsafe] Option#get is disabled - use Option#fold instead",
           "[org.wartremover.warts.Unsafe] var is disabled"), "result.errors")(result.errors.toSet)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
