package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class CompanionTest extends FunSuite {
  test("can use companion objects for case classes") {
    val result = WartTestTraverser(Unsafe) {
      case class Foo(n: Int)
      object Foo
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can use companion objects for type aliases") {
    val result = WartTestTraverser(Unsafe) {
      trait T[R]
      type T1 = String
      object T1 extends T[Unit]
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
