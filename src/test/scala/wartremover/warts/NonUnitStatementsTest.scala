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
    assert(result.errors == List("Statements must return Unit"))
    assert(result.warnings == List.empty)
  }
}
