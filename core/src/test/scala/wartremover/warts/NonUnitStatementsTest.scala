package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.NonUnitStatements

class NonUnitStatementsTest extends FunSuite {
  test("non-unit statements are disabled") {
    val result = WartTestTraverser(NonUnitStatements) {
      1
      2
    }
    assertResult(List("Statements must return Unit"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Extending a class with multiple parameter lists doesn't fail") {
    val result = WartTestTraverser(NonUnitStatements) {
      class A(x: Int)(y: Int)(z: Int)
      class B(x: Int)(y: Int)(z: Int) extends A(x)(y)(z)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("XML literals don't fail") {
    val result = WartTestTraverser(NonUnitStatements) {
      val a = 13
      <x>{a}</x>
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("NonUnitStatements wart obeys SuppressWarnings") {
    val result = WartTestTraverser(NonUnitStatements) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.NonUnitStatements"))
      val foo = {
        1
        2
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
