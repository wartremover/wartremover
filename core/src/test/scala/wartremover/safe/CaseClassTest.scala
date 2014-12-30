package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class CaseClassTest extends FunSuite {
  test("case classes still work") {
    val result = WartTestTraverser(Unsafe) {
      case class A(a: Int)
      case class B[X](a: X)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("vararg case classes still work") {
    val result = WartTestTraverser(Unsafe) {
      case class A(a: Int*)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
