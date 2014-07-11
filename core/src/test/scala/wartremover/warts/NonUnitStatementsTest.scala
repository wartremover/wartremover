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
    expectResult(List("Statements must return Unit"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("XML literals don't fail") {
    val result = WartTestTraverser(NonUnitStatements) {
      val a = 13
      <x>{a}</x>
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
